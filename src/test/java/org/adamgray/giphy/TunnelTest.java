package org.adamgray.giphy;

import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


/**
 * Unit test for Tunnel
 */
public class TunnelTest {

    @Test
    public void testNullArguments() {
        try {
            new Tunnel(mock(Proxy.class),null, mock(Socket.class));
            Assert.fail("Should not allow null parameters");
        } catch (final IllegalArgumentException e) {
        }

        try {
            new Tunnel(mock(Proxy.class), mock(Socket.class), null);
            Assert.fail("Should not allow null parameters");
        } catch (final IllegalArgumentException e) {
        }

        try {
            new Tunnel(null, mock(Socket.class), mock(Socket.class));
            Assert.fail("Should not allow null parameters");
        } catch (final IllegalArgumentException e) {
        }
    }

    @Test
    public void testSimplePayload() throws Exception {
        final String payload = "payload";

        final Socket local = mock(Socket.class);
        final Socket remote = mock(Socket.class);

        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        when(local.getInputStream()).thenReturn(new ByteArrayInputStream(payload.getBytes(StandardCharsets.UTF_8)));
        when(remote.getOutputStream()).thenReturn(byteArrayOutputStream);

        final Tunnel tunnel = new Tunnel(mock(Proxy.class), local, remote);
        tunnel.run();

        Assert.assertEquals(new String(payload.getBytes(), StandardCharsets.UTF_8),
                new String(byteArrayOutputStream.toByteArray(), StandardCharsets.UTF_8));
    }

    @Test
    public void testExceptionalPayload() throws Exception {
        final Socket local = mock(Socket.class);
        final Socket remote = mock(Socket.class);

        CheckIfThrownException exception = new CheckIfThrownException("injected exception");
        when(local.getInputStream()).thenThrow(exception);

        final Tunnel tunnel = new Tunnel(mock(Proxy.class), local, remote);
        tunnel.run();

        Assert.assertTrue(exception.getMessageCalled());
    }
}
