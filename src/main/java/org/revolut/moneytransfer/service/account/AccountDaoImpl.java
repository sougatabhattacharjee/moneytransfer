package org.revolut.moneytransfer.service.account;

import com.google.common.collect.Maps;
import org.revolut.moneytransfer.domain.Account;
import org.revolut.moneytransfer.domain.AccountStatus;
import org.revolut.moneytransfer.domain.Money;
import org.revolut.moneytransfer.domain.User;
import org.revolut.moneytransfer.domain.request.AccountBalanceRequest;
import org.revolut.moneytransfer.domain.request.AccountInfoRequest;
import org.revolut.moneytransfer.domain.request.AccountRequest;
import org.revolut.moneytransfer.domain.request.AccountStatusRequest;
import org.revolut.moneytransfer.exception.AccountAccessException;
import org.revolut.moneytransfer.exception.AccountNotFoundException;
import org.revolut.moneytransfer.exception.CurrencyException;
import org.revolut.moneytransfer.exception.CurrencyNotMatchingException;
import org.revolut.moneytransfer.exception.NegativeBalanceException;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * Created by Sougata Bhattacharjee
 * On 15.09.18
 */
public class AccountDaoImpl implements AccountDao {

    private final static Map<Long, Account> accounts = Maps.newConcurrentMap();
    private final AtomicLong maxAccountId = new AtomicLong(0);

    public AccountDaoImpl() {
    }

    public AccountDaoImpl(final Map<Long, Account> accounts) {
        this.accounts.putAll(accounts);
    }

    @Override
    public synchronized Account insertNewAccount(final Account account) {
        final long newAccountId = maxAccountId.incrementAndGet();
        final Account newAccount = new Account(newAccountId, account);

        accounts.put(newAccount.getAccountId(), newAccount);
        return newAccount;
    }

    @Override
    public List<Account> getAllAccounts(final Optional<AccountStatus> status) {

        if (status.isPresent())
            return accounts.values().stream()
                    .filter(ac -> ac.getStatus() == status.get())
                    .sorted(Comparator.comparing(Account::getCreated).reversed())
                    .collect(Collectors.toList());
        else
            return accounts.values().stream()
                    .sorted(Comparator.comparing(Account::getCreated).reversed())
                    .collect(Collectors.toList());
    }

    @Override
    public Account getAccountById(final Long accountID) throws AccountNotFoundException {

        final Account account = getAccount(accountID);

        if (account.getStatus() == AccountStatus.INACTIVE)
            throw new AccountNotFoundException("Account id: [" + accountID + "] is INACTIVE");

        return account;

    }

    @Override
    public synchronized Account updateAccountBalance(AccountBalanceRequest request, boolean isCredit)
            throws AccountNotFoundException, CurrencyException {
        final Account accountToUpdate = getAccountById(request.getAccountId());
        final Money amount = request.getBalance();
        if (amount.getCurrency() != accountToUpdate.getBalance().getCurrency())
            throw new CurrencyNotMatchingException("Currency is not matching for: " +
                    "" + amount.getCurrency() + " and " + accountToUpdate.getBalance().getCurrency() + "");
        if (isCredit) {
            accountToUpdate.getBalance().setAmount(accountToUpdate.getBalance().getAmount().add(amount.getAmount()));
            accounts.replace(accountToUpdate.getAccountId(), accountToUpdate);
            return accountToUpdate;
        } else {
            final BigDecimal result = accountToUpdate.getBalance().getAmount().subtract(amount.getAmount());
            if (result.compareTo(BigDecimal.ZERO) < 0)
                throw new NegativeBalanceException("Do not have sufficient fund to perform debit operation," +
                        " current balance: " + accountToUpdate.getBalance().getAmount() +
                        " " + accountToUpdate.getBalance().getCurrency());
            accountToUpdate.getBalance().setAmount(result);
            accounts.replace(accountToUpdate.getAccountId(), accountToUpdate);
            return accountToUpdate;
        }

    }


    @Override
    public synchronized Account updateAccountById(AccountRequest accountRequest) throws AccountAccessException {
        final AccountInfoRequest accountInfoRequestToUpdate;
        final AccountStatusRequest accountStatusRequestToUpdate;

        if (accountRequest instanceof AccountInfoRequest) {
            accountInfoRequestToUpdate = (AccountInfoRequest) accountRequest;
            final Account toUpdate = getAccountById(accountInfoRequestToUpdate.getAccountId());
            if (!toUpdate.getAccountHolder().getName().equals(accountInfoRequestToUpdate.getAccountHolder().getName())) {
                toUpdate.setAccountHolder(new User(accountInfoRequestToUpdate.getAccountHolder().getName()));
                accounts.replace(toUpdate.getAccountId(), toUpdate);
            }
            return toUpdate;

        } else if (accountRequest instanceof AccountStatusRequest) {
            accountStatusRequestToUpdate = (AccountStatusRequest) accountRequest;
            final Account toUpdate = getAccount(accountStatusRequestToUpdate.getAccountId());
            if (!toUpdate.getStatus().equals(accountStatusRequestToUpdate.getStatus())) {
                toUpdate.setStatus(accountStatusRequestToUpdate.getStatus());
                accounts.replace(toUpdate.getAccountId(), toUpdate);
            }
            return toUpdate;

        } else throw new AccountAccessException("Malformed Request");

    }

    private Account getAccount(final Long accountID) throws AccountNotFoundException {
        return accounts.entrySet().stream()
                .filter(e -> e.getKey().equals(accountID))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElseThrow(() -> new AccountNotFoundException(accountID));
    }


}
