package org.adamgray.giphy;

import org.apache.http.HttpHost;
import org.apache.http.HttpVersion;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.*;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

public class GiphyProxyIntegrationTest {
    private static final Logger logger = LogManager.getLogger(GiphyProxyIntegrationTest.class);

    private static final String GIPHY_API_KEY = "O4FTCFzVGQFAhaCzS9uFD5NFBY8tt14I";
    private static final ExecutorService EXECUTOR = Executors.newCachedThreadPool();
    private static final Thread MAIN_RUNNER = new Thread(() -> {
        try {
            GiphyProxy.main(new String[]{"1312", "api.giphy.com:443"});
        } catch (Exception e) {
            Assert.fail("Uncaught exception: " + e.getMessage());
        }
    });

    @BeforeClass
    public static void beforeClass() {
        if(!MAIN_RUNNER.isAlive()) {
            MAIN_RUNNER.start();
        }
    }

    @Test
    public void simpleIntegrationTest() throws Exception {
        try (CloseableHttpClient client = getHttpClient()) {
            HttpGet request = new HttpGet(getGiphyUri("kitten"));
            assertResponse(client.execute(request));
        }
    }

    @Test
    public void successFailSuccessIntegrationTest() throws Exception {
        try (CloseableHttpClient client = getHttpClient()) {
            HttpGet request = new HttpGet(getGiphyUri("kitten"));
            assertResponse(client.execute(request));
            HttpGet second = new HttpGet(new URIBuilder("https://www.google.com").build());
            assertErrorResponse(client.execute(second), 502, "Bad Gateway");
            HttpGet third = new HttpGet(getGiphyUri("puppy"));
            assertResponse(client.execute(third));
        }
    }

    @Test
    public void disallowedRemoteIntegrationTest() throws Exception {
        try (CloseableHttpClient client = getHttpClient()) {
            HttpGet request = new HttpGet(new URIBuilder("https://www.google.com").build());
            assertErrorResponse(client.execute(request), 502, "Bad Gateway");
        }
    }

    @Test
    public void disallowedVerbIntegrationTest() throws Exception {
        // this client doesn't have the proxy
        try (CloseableHttpClient client = getHttpClientBuilder().build()) {
            HttpGet request = new HttpGet(new URIBuilder().setScheme("http").setHost("localhost").setPort(1312).build());
            assertErrorResponse(client.execute(request), 405, "Method Not Allowed");
        }
    }

    @Test
    public void simpleConcurrentIntegrationTest() throws Exception {
        try (CloseableHttpClient client = getHttpClient()) {
            final List<String> queries = Arrays.asList("cat", "dog", "horse", "cow", "rabbit");
            final List<Future<CloseableHttpResponse>> futures = queries.stream()
                    .map(query -> EXECUTOR.submit(getRequestRunnable(new HttpGet(getGiphyUri(query)), client)))
                    .collect(Collectors.toList());

            for (Future<CloseableHttpResponse> future : futures) {
                assertResponse(future.get());
            }
        }
    }

    private final Callable<CloseableHttpResponse> getRequestRunnable(final HttpGet httpGet, final CloseableHttpClient client) {
        return () -> client.execute(httpGet);
    }

    private HttpClientBuilder getHttpClientBuilder() {
        return HttpClientBuilder.create()
                .setMaxConnTotal(5) // apache httpclient is very conservative about concurrency
                .setMaxConnPerRoute(5);
    }

    private CloseableHttpClient getHttpClient() {
        final HttpHost proxy = new HttpHost("localhost", 1312, "http");
        return getHttpClientBuilder().setProxy(proxy).build();
    }

    private void assertErrorResponse(final CloseableHttpResponse response, int statusCode, String reasonPhrase) throws IOException {
        final StatusLine statusLine = response.getStatusLine();
        Assert.assertEquals(HttpVersion.HTTP_1_1, statusLine.getProtocolVersion());
        Assert.assertEquals(statusCode, statusLine.getStatusCode());
        Assert.assertEquals(reasonPhrase, statusLine.getReasonPhrase());
        response.close();
    }

    private void assertResponse(final CloseableHttpResponse response) throws IOException {
        final StatusLine statusLine = response.getStatusLine();
        Assert.assertEquals(HttpVersion.HTTP_1_1, statusLine.getProtocolVersion());
        Assert.assertEquals(200, statusLine.getStatusCode());
        Assert.assertEquals("OK", statusLine.getReasonPhrase());
        Assert.assertEquals("application/json", response.getEntity().getContentType().getValue());
        String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
        logger.info(responseBody);
        response.close();
    }

    private URI getGiphyUri(final String query) {
        try {
            final URIBuilder uriBuilder = new URIBuilder("https://api.giphy.com/v1/gifs/search");
            uriBuilder.setParameter("api_key", GIPHY_API_KEY);
            uriBuilder.setParameter("q", query);
            uriBuilder.setParameter("limit", "1");
            uriBuilder.setParameter("offset", "0");
            uriBuilder.setParameter("rating", "g");
            uriBuilder.setParameter("lang", "en");
            return uriBuilder.build();
        } catch (final URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
