package org.revolut.moneytransfer.exception;

/**
 * Created by Sougata Bhattacharjee
 * On 16.09.18
 */
public class NegativeBalanceException extends CurrencyException {
    public NegativeBalanceException(final String msg) {
        super(msg);
    }
}