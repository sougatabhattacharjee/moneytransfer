package org.revolut.moneytransfer.service.transfer;

import org.revolut.moneytransfer.domain.MoneyTransfer;
import org.revolut.moneytransfer.exception.AccountNotFoundException;
import org.revolut.moneytransfer.exception.MoneyTransferWebServiceException;

import java.util.List;

/**
 * Created by Sougata Bhattacharjee
 * On 16.09.18
 */
public interface MoneyTransferDao {

    MoneyTransfer newMoneyTransfer(MoneyTransfer request)
            throws AccountNotFoundException, MoneyTransferWebServiceException;

    List<MoneyTransfer> getAllTransfers();

    List<MoneyTransfer> getAllTransfersByAccount(long accountId);
}
