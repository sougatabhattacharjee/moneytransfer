package org.revolut.moneytransfer.service.transfer;

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import org.joda.time.DateTime;
import org.revolut.moneytransfer.domain.Account;
import org.revolut.moneytransfer.domain.Currency;
import org.revolut.moneytransfer.domain.Money;
import org.revolut.moneytransfer.domain.MoneyTransfer;
import org.revolut.moneytransfer.exception.AccountNotFoundException;
import org.revolut.moneytransfer.exception.MoneyTransferWebServiceException;
import org.revolut.moneytransfer.service.account.AccountDao;
import org.revolut.moneytransfer.service.account.AccountDaoImpl;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Sougata Bhattacharjee
 * On 16.09.18
 */
public class MoneyTransferDaoImpl implements MoneyTransferDao {

    private final AccountDao accountDao;
    private final List<MoneyTransfer> moneyTransfers;

    public MoneyTransferDaoImpl() {
        this.accountDao = new AccountDaoImpl();
        this.moneyTransfers = Lists.newCopyOnWriteArrayList();
    }

    public MoneyTransferDaoImpl(final List<MoneyTransfer> moneyTransfers, final AccountDaoImpl accountDao) {
        this.accountDao = accountDao;
        this.moneyTransfers = Lists.newCopyOnWriteArrayList();
        this.moneyTransfers.addAll(moneyTransfers);
    }


    @Override
    public MoneyTransfer newMoneyTransfer(final MoneyTransfer request) throws AccountNotFoundException, MoneyTransferWebServiceException {
        verifyTransferRequest(request);
        final Account sourceAccount = accountDao.getAccountById(request.getSourceAccountId());
        final Account destinationAccount = accountDao.getAccountById(request.getDestinationAccountId());

        sourceAccount.getBalance().setAmount(sourceAccount.getBalance().getAmount()
                .subtract(request.getAmount().getAmount()));

        destinationAccount.getBalance().setAmount(destinationAccount.getBalance().getAmount()
                .add(request.getAmount().getAmount()));

        final MoneyTransfer transfer = new MoneyTransfer();
        transfer.setAmount(request.getAmount());
        transfer.setSourceAccountId(request.getSourceAccountId());
        transfer.setDestinationAccountId(request.getDestinationAccountId());
        transfer.setTransferDate(DateTime.now());
        transfer.setDescription(request.getDescription());
        moneyTransfers.add(transfer);
        return transfer;
    }

    @Override
    public List<MoneyTransfer> getAllTransfers() {
        return moneyTransfers.stream()
                .sorted(Comparator.comparing(MoneyTransfer::getTransferDate).reversed())
                .collect(Collectors.toList());
    }

    @Override
    public List<MoneyTransfer> getAllTransfersByAccount(final long accountId) {
        return moneyTransfers.stream()
                .filter(ts -> ts.getSourceAccountId() == accountId)
                .sorted(Comparator.comparing(MoneyTransfer::getTransferDate).reversed())
                .collect(Collectors.toList());
    }


    private final Predicate<MoneyTransfer> IS_SOURCE_DESTINATION_ACCOUNT_DIFFERENT =
            new Predicate<MoneyTransfer>() {
                @Override
                public boolean apply(final MoneyTransfer request) {
                    return request.getSourceAccountId() != request.getDestinationAccountId();
                }
            };

    private final Predicate<MoneyTransfer> IS_SOURCE_ACCOUNT_EXISTS =
            new Predicate<MoneyTransfer>() {
                @Override
                public boolean apply(final MoneyTransfer request) {
                    try {
                        accountDao.getAccountById(request.getSourceAccountId());
                    } catch (AccountNotFoundException e) {
                        return false;
                    }
                    return true;
                }
            };

    private final Predicate<MoneyTransfer> IS_DESTINATION_ACCOUNT_EXISTS =
            new Predicate<MoneyTransfer>() {
                @Override
                public boolean apply(final MoneyTransfer request) {
                    try {
                        accountDao.getAccountById(request.getDestinationAccountId());
                    } catch (AccountNotFoundException e) {
                        return false;
                    }
                    return true;
                }
            };

    private final Predicate<MoneyTransfer> IS_SOURCE_DESTINATION_ACCOUNT_HAS_SIMILAR_CURRENCY =
            new Predicate<MoneyTransfer>() {
                @Override
                public boolean apply(final MoneyTransfer request) {
                    try {
                        final Currency currency =
                                accountDao.getAccountById(request.getDestinationAccountId()).getBalance().getCurrency();
                        return currency == request.getAmount().getCurrency();
                    } catch (AccountNotFoundException e) {
                        return false;
                    }
                }
            };

    private final Predicate<MoneyTransfer> IS_SOURCE_ACCOUNT_HAS_SUFFICIENT_BALANCE =
            new Predicate<MoneyTransfer>() {
                @Override
                public boolean apply(final MoneyTransfer request) {
                    try {
                        final Money balance = accountDao.getAccountById(request.getSourceAccountId()).getBalance();
                        return balance.getAmount().subtract(request.getAmount().getAmount()).compareTo(BigDecimal.ZERO) >= 0;
                    } catch (AccountNotFoundException e) {
                        return false;
                    }
                }
            };


    private void verifyTransferRequest(final MoneyTransfer request) throws MoneyTransferWebServiceException {
        if (!IS_SOURCE_DESTINATION_ACCOUNT_DIFFERENT.apply(request))
            throw new MoneyTransferWebServiceException("Source and destination account must be different");
        else if (!IS_SOURCE_ACCOUNT_EXISTS.apply(request))
            throw new MoneyTransferWebServiceException("Source account does not exist");
        else if (!IS_DESTINATION_ACCOUNT_EXISTS.apply(request))
            throw new MoneyTransferWebServiceException("Destination account does not exist");
        else if (!IS_SOURCE_DESTINATION_ACCOUNT_HAS_SIMILAR_CURRENCY.apply(request))
            throw new MoneyTransferWebServiceException(
                    "Cannot perform transfer, Source and Destination account have different Currency");
        else if (!IS_SOURCE_ACCOUNT_HAS_SUFFICIENT_BALANCE.apply(request))
            throw new MoneyTransferWebServiceException(
                    "Cannot perform transfer, Source Account does not have sufficient fund");
    }
}
