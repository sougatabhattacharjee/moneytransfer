package org.revolut.moneytransfer.domain.request;

import lombok.Data;
import org.revolut.moneytransfer.domain.Money;

/**
 * Created by Sougata Bhattacharjee
 * On 16.09.18
 */
@Data
public class AccountBalanceRequest extends AccountRequest {

    private Long accountId;
    private Money balance;
}
