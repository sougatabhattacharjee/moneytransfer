package org.revolut.moneytransfer.exception;

/**
 * Created by Sougata Bhattacharjee
 * On 15.09.18
 */
public class AccountAccessException extends Exception {
    public AccountAccessException(String message) {
        super(message);
    }
}
