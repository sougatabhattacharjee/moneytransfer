package org.revolut.moneytransfer.exception;

/**
 * Created by Sougata Bhattacharjee
 * On 15.09.18
 */
public class CurrencyException extends AccountAccessException {
    public CurrencyException(String message) {
        super(message);
    }
}
