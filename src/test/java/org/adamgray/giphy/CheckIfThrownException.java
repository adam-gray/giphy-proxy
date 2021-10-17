package org.adamgray.giphy;

import java.io.IOException;

public class CheckIfThrownException extends IOException {
    boolean getMessageCalled = false;

    public CheckIfThrownException(final String message) {
        super(message);
    }

    @Override
    public String getMessage() {
        getMessageCalled = true;
        return super.getMessage();
    }

    boolean getMessageCalled() {
        return this.getMessageCalled;
    }
}
