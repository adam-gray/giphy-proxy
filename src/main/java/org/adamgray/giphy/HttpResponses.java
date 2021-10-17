package org.adamgray.giphy;

public final class HttpResponses {
    private HttpResponses() {}

    public static final String OK = "HTTP/1.1 200 OK";
    public static final String BAD_REQUEST = "HTTP/1.1 400 Bad Request";
    public static final String METHOD_NOT_ALLOWED = "HTTP/1.1 405 Method Not Allowed";
    public static final String BAD_GATEWAY = "HTTP/1.1 502 Bad Gateway";
}
