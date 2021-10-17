package org.adamgray.giphy;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GiphyProxy {
    private static final ExecutorService executorService = Executors.newCachedThreadPool();
    private static final Logger logger = LogManager.getLogger(GiphyProxy.class);

    public static void main(String[] args) throws Exception {
        try {
            if (args == null) {
                throw new IllegalArgumentException("Input arguments cannot be null");
            } else if (args.length != 2) {
                throw new IllegalArgumentException("Expected exactly two arguments (local port and host:port of allowed endpoint");
            }
            final int localPort = Integer.parseInt(args[0]);
            if (localPort <= 0) {
                throw new IllegalArgumentException("Port must be a positive integer");
            }
            final InetSocketAddress allowedEndpoint = parseInetSocketAddress(args[1]);
            logger.debug("Opening socket on port: {}", localPort);
            try (final ServerSocket serverSocket = new ServerSocket(localPort)) {
            // loop infinitely until you ctrl-c the client
            while (true) {
                Socket localSocket = serverSocket.accept();
                try {
                    logger.debug("Socket established.");
                    final ConnectHandler connectHandler = new ConnectHandler(localSocket, allowedEndpoint);
                    final InetSocketAddress remote = connectHandler.handle();
                    final Socket remoteSocket = new Socket(remote.getHostName(), remote.getPort());
                    final Proxy proxy = new Proxy(localSocket, remoteSocket);
                    proxy.start();
                } catch (final IOException | IllegalArgumentException e) {
                    logger.error("Exception while handling request, closing socket.");
                    localSocket.close();
                }
            }
            }
        } finally {
            executorService.shutdown();
        }
    }

    public static InetSocketAddress parseInetSocketAddress(final String hostAndPort) {
        if (hostAndPort == null) {
            throw new IllegalArgumentException("String must not be null");
        }
        final String[] split = hostAndPort.split(":");
        if (split.length != 2) {
            throw new IllegalArgumentException("String must be formatted host:port");
        }
        final String host = split[0];
        final int port = Integer.parseInt(split[1]);
        return InetSocketAddress.createUnresolved(host, port);
    }
}
