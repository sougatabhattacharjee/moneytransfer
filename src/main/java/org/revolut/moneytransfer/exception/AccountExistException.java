package org.revolut.moneytransfer.exception;

/**
 * Created by Sougata Bhattacharjee
 * On 15.09.18
 */
public class AccountExistException extends AccountAccessException {
    public AccountExistException(final Long accountId) {
        super("Account " + accountId + " already exists");
    }
}
