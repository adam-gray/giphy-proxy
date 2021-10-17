package org.adamgray.giphy;

import org.junit.Test;

public class GiphyProxyTest {

    @Test(expected = IllegalArgumentException.class)
    public void testNullInputArguments() throws Exception {
        GiphyProxy.main(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEmptyInputArguments() throws Exception {
        GiphyProxy.main(new String[]{});
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidPortArgument() throws Exception {
        GiphyProxy.main(new String[]{"not_an_integer", "api.giphy.com:443"});
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNegativePortArgument() throws Exception {
        GiphyProxy.main(new String[]{"-1312", "api.giphy.com:443"});
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidEndpointArgument() throws Exception {
        GiphyProxy.main(new String[]{"1312", "https://api.giphy.com"});
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullInetSocketAddress() {
        GiphyProxy.parseInetSocketAddress(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidStringForInetSocketAddress() throws Exception {
        GiphyProxy.parseInetSocketAddress("invalid.com");
    }

}
