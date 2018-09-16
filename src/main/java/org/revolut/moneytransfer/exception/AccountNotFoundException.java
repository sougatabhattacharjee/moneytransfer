package org.revolut.moneytransfer.exception;

/**
 * Created by Sougata Bhattacharjee
 * On 15.09.18
 */
public class AccountNotFoundException extends AccountAccessException {
    public AccountNotFoundException(final Long accountId) {
        super("Could not find account for id [" + accountId + "]");
    }

    public AccountNotFoundException(final String msg) {
        super(msg);
    }
}
