package org.revolut.moneytransfer.exception;

/**
 * Created by Sougata Bhattacharjee
 * On 16.09.18
 */
public class CurrencyNotMatchingException extends CurrencyException {
    public CurrencyNotMatchingException(final String msg) {
        super(msg);
    }
}
