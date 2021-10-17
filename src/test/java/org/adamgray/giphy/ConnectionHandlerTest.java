package org.adamgray.giphy;

import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

import static org.adamgray.giphy.HttpResponses.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ConnectionHandlerTest {
    private static final String TWO_CARRIAGE_RETURNS = "\r\n\r\n";
    private static final String API_GIPHY_COM_443 = "api.giphy.com:443";

    @Test
    public void testNullArguments() {
        final InetSocketAddress inetSocketAddress = GiphyProxy.parseInetSocketAddress(API_GIPHY_COM_443);
        try {
            new ConnectHandler(null, inetSocketAddress);
            Assert.fail("Should not allow null parameters");
        } catch (final IllegalArgumentException e) {
        }

        try {
            new ConnectHandler(mock(Socket.class), null);
            Assert.fail("Should not allow null parameters");
        } catch (final IllegalArgumentException e) {
        }
    }

    @Test
    public void testSuccessfulConnect() throws Exception {
        final String expectedResponse = OK + TWO_CARRIAGE_RETURNS;
        final String connectRequest = "CONNECT " + API_GIPHY_COM_443 + " HTTP/1.1";
        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        final Socket mockLocalSocket = mock(Socket.class);
        when(mockLocalSocket.getInputStream()).thenReturn(
                new ByteArrayInputStream(connectRequest.getBytes(StandardCharsets.UTF_8)));
        when(mockLocalSocket.getOutputStream()).thenReturn(byteArrayOutputStream);

        final ConnectHandler connectHandler = new ConnectHandler(mockLocalSocket, GiphyProxy.parseInetSocketAddress(API_GIPHY_COM_443));
        connectHandler.handle();

        Assert.assertEquals(new String(expectedResponse.getBytes(), StandardCharsets.UTF_8),
                new String(byteArrayOutputStream.toByteArray(), StandardCharsets.UTF_8));
    }

    @Test(expected = IOException.class)
    public void testIOErrorOnWritingResponse() throws Exception {
        final String expectedResponse = OK + TWO_CARRIAGE_RETURNS;
        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        final Socket mockLocalSocket = mock(Socket.class);
        when(mockLocalSocket.getInputStream()).thenThrow(new IOException("injected exception"));
        when(mockLocalSocket.getOutputStream()).thenReturn(byteArrayOutputStream);

        final ConnectHandler connectHandler = new ConnectHandler(mockLocalSocket, GiphyProxy.parseInetSocketAddress(API_GIPHY_COM_443));
        connectHandler.handle();

        Assert.assertEquals(new String(expectedResponse.getBytes(), StandardCharsets.UTF_8),
                new String(byteArrayOutputStream.toByteArray(), StandardCharsets.UTF_8));
    }

    @Test(expected = ErrorResponseIOException.class)
    public void testInvalidRequest() throws Exception {
        final String expectedResponse = METHOD_NOT_ALLOWED + TWO_CARRIAGE_RETURNS;
        final String invalidRequest = "GET " + API_GIPHY_COM_443 + " HTTP/1.1";
        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        final Socket mockLocalSocket = mock(Socket.class);
        when(mockLocalSocket.getInputStream()).thenReturn(
                new ByteArrayInputStream(invalidRequest.getBytes(StandardCharsets.UTF_8)));
        when(mockLocalSocket.getOutputStream()).thenReturn(byteArrayOutputStream);

        final ConnectHandler connectHandler = new ConnectHandler(mockLocalSocket, GiphyProxy.parseInetSocketAddress(API_GIPHY_COM_443));
        connectHandler.handle();

        Assert.assertEquals(new String(expectedResponse.getBytes(), StandardCharsets.UTF_8),
                new String(byteArrayOutputStream.toByteArray(), StandardCharsets.UTF_8));
    }

    @Test(expected = ErrorResponseIOException.class)
    public void testInvalidHost() throws Exception {
        final String expectedResponse = BAD_REQUEST + TWO_CARRIAGE_RETURNS;
        final String invalidHost = "https://api.giphy.com";
        final String invalidRequest = "CONNECT " + invalidHost + " HTTP/1.1";
        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        final Socket mockLocalSocket = mock(Socket.class);
        when(mockLocalSocket.getInputStream()).thenReturn(
                new ByteArrayInputStream(invalidRequest.getBytes(StandardCharsets.UTF_8)));
        when(mockLocalSocket.getOutputStream()).thenReturn(byteArrayOutputStream);

        final ConnectHandler connectHandler = new ConnectHandler(mockLocalSocket, GiphyProxy.parseInetSocketAddress(API_GIPHY_COM_443));
        connectHandler.handle();

        Assert.assertEquals(new String(expectedResponse.getBytes(), StandardCharsets.UTF_8),
                new String(byteArrayOutputStream.toByteArray(), StandardCharsets.UTF_8));
    }

    @Test(expected = ErrorResponseIOException.class)
    public void testDisallowedHost() throws Exception {
        final String expectedResponse = BAD_GATEWAY + TWO_CARRIAGE_RETURNS;
        final String invalidHost = "www.google.com:443";
        final String invalidRequest = "CONNECT " + invalidHost + " HTTP/1.1";
        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        final Socket mockLocalSocket = mock(Socket.class);
        when(mockLocalSocket.getInputStream()).thenReturn(
                new ByteArrayInputStream(invalidRequest.getBytes(StandardCharsets.UTF_8)));
        when(mockLocalSocket.getOutputStream()).thenReturn(byteArrayOutputStream);

        final ConnectHandler connectHandler = new ConnectHandler(mockLocalSocket, GiphyProxy.parseInetSocketAddress(API_GIPHY_COM_443));
        connectHandler.handle();

        Assert.assertEquals(new String(expectedResponse.getBytes(), StandardCharsets.UTF_8),
                new String(byteArrayOutputStream.toByteArray(), StandardCharsets.UTF_8));
    }

    @Test(expected = ErrorResponseIOException.class)
    public void testInvalidFormattedRequest() throws Exception {
        final String expectedResponse = BAD_REQUEST + TWO_CARRIAGE_RETURNS;
        final String invalidRequest = "CONNECT " + API_GIPHY_COM_443;
        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        final Socket mockLocalSocket = mock(Socket.class);
        when(mockLocalSocket.getInputStream()).thenReturn(
                new ByteArrayInputStream(invalidRequest.getBytes(StandardCharsets.UTF_8)));
        when(mockLocalSocket.getOutputStream()).thenReturn(byteArrayOutputStream);

        final ConnectHandler connectHandler = new ConnectHandler(mockLocalSocket, GiphyProxy.parseInetSocketAddress(API_GIPHY_COM_443));
        connectHandler.handle();

        Assert.assertEquals(new String(expectedResponse.getBytes(), StandardCharsets.UTF_8),
                new String(byteArrayOutputStream.toByteArray(), StandardCharsets.UTF_8));
    }
}
