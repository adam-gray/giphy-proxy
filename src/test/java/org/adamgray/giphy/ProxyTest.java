package org.adamgray.giphy;

import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit test for Proxy
 */
public class ProxyTest {
    @Test
    public void testNullArguments() {
        try {
            new Proxy(null, mock(Socket.class));
            Assert.fail("Should not allow null parameters");
        } catch (final IllegalArgumentException e) {
        }

        try {
            new Proxy(mock(Socket.class), null);
            Assert.fail("Should not allow null parameters");
        } catch (final IllegalArgumentException e) {
        }
    }

    @Test
    public void testSimplePayload() throws Exception {
        final String localPayload = "local_payload";
        final String remotePayload = "remote_payload";

        final Socket local = mock(Socket.class);
        final Socket remote = mock(Socket.class);

        final ByteArrayOutputStream localOutputStream = new ByteArrayOutputStream();
        final ByteArrayOutputStream remoteOutputStream = new ByteArrayOutputStream();

        when(local.getInputStream()).thenReturn(new ByteArrayInputStream(localPayload.getBytes(StandardCharsets.UTF_8)));
        when(local.getOutputStream()).thenReturn(localOutputStream);

        when(remote.getInputStream()).thenReturn(new ByteArrayInputStream(remotePayload.getBytes(StandardCharsets.UTF_8)));
        when(remote.getOutputStream()).thenReturn(remoteOutputStream);

        final ExecutorService executorService = Executors.newCachedThreadPool();
        final Proxy proxy = new Proxy(local, remote);
        proxy.start();
        executorService.awaitTermination(100, TimeUnit.MILLISECONDS);

        Assert.assertEquals(new String(localPayload.getBytes(), StandardCharsets.UTF_8),
                new String(remoteOutputStream.toByteArray(), StandardCharsets.UTF_8));

        Assert.assertEquals(new String(remotePayload.getBytes(), StandardCharsets.UTF_8),
                new String(localOutputStream.toByteArray(), StandardCharsets.UTF_8));
    }
}
