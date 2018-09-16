package org.revolut.moneytransfer.service;

import org.revolut.moneytransfer.domain.Account;
import org.revolut.moneytransfer.domain.AccountStatus;
import org.revolut.moneytransfer.domain.request.AccountBalanceRequest;
import org.revolut.moneytransfer.domain.request.AccountRequest;
import org.revolut.moneytransfer.domain.request.AccountStatusRequest;
import org.revolut.moneytransfer.exception.AccountAccessException;
import org.revolut.moneytransfer.exception.AccountExistException;
import org.revolut.moneytransfer.exception.AccountNotFoundException;
import org.revolut.moneytransfer.exception.CurrencyException;
import org.revolut.moneytransfer.exception.CurrencyNotMatchingException;

import java.util.List;
import java.util.Optional;

/**
 * Created by Sougata Bhattacharjee
 * On 15.09.18
 */
public interface AccountDao {

    Account insertNewAccount(final Account account);

    List<Account> getAllAccounts(Optional<AccountStatus> status);

    Account getAccountById(Long accountID) throws AccountNotFoundException;

    Account updateAccountBalance(AccountBalanceRequest request, boolean isCredit)
            throws AccountNotFoundException, CurrencyException;

    Account updateAccountById(AccountRequest accountRequest) throws AccountAccessException;
}
