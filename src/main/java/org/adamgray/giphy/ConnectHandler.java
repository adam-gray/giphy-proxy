package org.adamgray.giphy;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;

import static org.adamgray.giphy.HttpResponses.*;

public class ConnectHandler {
    private static final Logger logger = LogManager.getLogger(ConnectHandler.class);

    private final Socket localSocket;
    private final InetSocketAddress allowedEndpoint;

     ConnectHandler(final Socket localSocket, final InetSocketAddress allowedEndpoint) {
        if (localSocket == null) {
            throw new IllegalArgumentException("Socket cannot be null");
        } else if (allowedEndpoint == null) {
            throw new IllegalArgumentException("Allowed endpoint cannot be null");
        }

        this.localSocket = localSocket;
        this.allowedEndpoint = allowedEndpoint;
    }

    /**
     * Handle the HTTP CONNECT request and response.
     *
     * @return HostAndPort of remote endpoint to tunnel & proxy to
     * @throws IOException - if error encountered writing status
     * @throws ErrorResponseIOException - exceptions for specific error cases
     */
    public InetSocketAddress handle() throws IOException {
        try {
            final InetSocketAddress hostAndPort = handleInternal();
            writeStatus(this.localSocket, OK);
            return hostAndPort;
        } catch (final ErrorResponseIOException e) {
            logger.error("Error while processing CONNECT: {} ", e.getMessage());
            writeStatus(this.localSocket, e.getMessage());
            localSocket.close();
            throw e;
        } catch (IOException e) {
            logger.error(e.getMessage());
            throw e;
        }
    }

    /**
     * Write out HTTP status to the client. Does not close the socket.
     *
     * @param socket       - socket to send statusString to
     * @param statusString - HTTP status string
     * @throws IOException - thrown from socket or from writer
     */
    private void writeStatus(final Socket socket, final String statusString) throws IOException {
        final BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        writer.write(statusString);
        // need 2 carriage returns to get clients to finish
        writer.write("\r\n\r\n");
        writer.flush();
    }


    /**
     * Handle the CONNECT request from the socket before tunneling
     * See rfc7231 section-4.3.6 for CONNECT spec.
     *
     * @throws ErrorResponseIOException - specific error message response
     * @throws IOException              - thrown from the socket or the reader / writer
     */
    private InetSocketAddress handleInternal() throws IOException {
        final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(this.localSocket.getInputStream()));
        final String firstLine = bufferedReader.readLine();
        logger.debug("{} from {}", firstLine, this.localSocket);
        final String[] strings = firstLine.split(" ");
        if (strings.length != 3) {
            logger.warn("Invalid request string!");
            bufferedReader.close();
            throw new ErrorResponseIOException(BAD_REQUEST);
        }
        final String verb = strings[0];
        if (verb.equals("CONNECT")) {
            final String hostAndPort = strings[1];
            try {
                final InetSocketAddress parsed = GiphyProxy.parseInetSocketAddress(hostAndPort);
                if (this.allowedEndpoint.equals(parsed)) {
                    return parsed;
                } else {
                    logger.warn("Address: {} not allowed", parsed);
                    throw new ErrorResponseIOException(BAD_GATEWAY);
                }
            } catch (IllegalArgumentException e) {
                logger.warn("Address: {} invalid for CONNECT", hostAndPort);
                throw new ErrorResponseIOException(BAD_REQUEST);
            }
        } else {
            logger.warn("{} not allowed", verb);
            throw new ErrorResponseIOException(METHOD_NOT_ALLOWED);
        }
    }
}
