package org.revolut.moneytransfer.config;

/**
 * Created by Sougata Bhattacharjee
 * On 15.09.18
 */
public class ResponseError {
    private String message;

    public ResponseError(final String message, final String... args) {
        this.message = String.format(message, args);
    }

    public ResponseError(Exception e) {
        this.message = e.getMessage();
    }

    public String getMessage() {
        return this.message;
    }
}
