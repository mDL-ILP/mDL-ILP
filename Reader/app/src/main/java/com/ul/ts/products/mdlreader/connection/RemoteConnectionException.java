package com.ul.ts.products.mdlreader.connection;

public class RemoteConnectionException extends Exception {
    public boolean retry;

    public RemoteConnectionException(String message, boolean retry) {
        super(message);
        this.retry = retry;
    }
}
