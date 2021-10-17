package org.adamgray.giphy;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class Tunnel extends Thread {
    private static final Logger logger = LogManager.getLogger(Tunnel.class);

    private final Socket local;
    private final Socket remote;
    private final Proxy proxy;

    /**
     * Implements a one way tunnel between two sockets.
     *
     *
     * @param proxy - parent proxy of the tunnel
     * @param local  - socket where data comes from
     * @param remote - socket where data ends up
     */
     Tunnel(final Proxy proxy, final Socket local, final Socket remote) {
        if (proxy == null) {
            throw new IllegalArgumentException("Proxy cannot be null");
        }else if (local == null) {
            throw new IllegalArgumentException("Local socket cannot be null");
        } else if (remote == null) {
            throw new IllegalArgumentException("Remote socket cannot be null");
        }
        this.proxy = proxy;
        this.local = local;
        this.remote = remote;
    }

    @Override
    public void run() {
        try (final InputStream inputStream = this.local.getInputStream();
             final OutputStream outputStream = this.remote.getOutputStream()) {
            logger.debug("Transferring data from {} to {}", this.local, this.remote);
            inputStream.transferTo(outputStream);
            outputStream.flush();
        } catch (final IOException e) {
            logger.error("Encountered exception: {}", e.getMessage());
            this.proxy.closeSockets();
        }
    }
}
