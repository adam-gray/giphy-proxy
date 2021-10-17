package org.adamgray.giphy;

import java.io.IOException;

/**
 * IOException subclass to pass back a specific HTTP status code & message
 */
public class ErrorResponseIOException extends IOException {
    ErrorResponseIOException(final String message) {
        super(message);
    }
}
