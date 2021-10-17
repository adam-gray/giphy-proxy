package org.adamgray.giphy;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.Socket;


public class Proxy extends Thread {
    private static final Logger logger = LogManager.getLogger(Proxy.class);

    private final Socket local;
    private final Socket remote;

    /**
     * Proxy that establishes a set of tunnels for bi-directional communication between sockets.
     *  @param local  - local socket (trying to securely proxy request)
     * @param remote - remote socket (somewhere where we want to obscure the local socket as source)
     */
    Proxy(final Socket local, final Socket remote) {
        if (local == null) {
            throw new IllegalArgumentException("Local socket cannot be null");
        } else if (remote == null) {
            throw new IllegalArgumentException("Remote socket cannot be null");
        }
        this.local = local;
        this.remote = remote;
    }

    @Override
    public void run() {
        logger.debug("Setting up tunnels for communication between: {} and  {}", this.local, this.remote);
        final Tunnel localToRemote = new Tunnel(this, this.local, this.remote);
            final Tunnel remoteToLocal = new Tunnel(this ,this.remote, this.local);
            localToRemote.start();
            remoteToLocal.start();
    }

    /**
     * One or both child tunnels may call this method in the error scenario.
     */
    public synchronized void closeSockets() {
        try {
            this.remote.close();
        } catch (Exception e) {
            logger.trace("Exception when closing {}, could be already closed", this.remote);
        }
        try {
            this.local.close();
        } catch (Exception e) {
            logger.trace("Exception when closing {}, could be already closed", this.local);
        }
    }

}
