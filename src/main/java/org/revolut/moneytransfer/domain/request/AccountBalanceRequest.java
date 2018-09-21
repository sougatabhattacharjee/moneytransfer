package org.revolut.moneytransfer.domain.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.revolut.moneytransfer.domain.Money;

/**
 * Created by Sougata Bhattacharjee
 * On 16.09.18
 */
@Data
public class AccountBalanceRequest extends AccountRequest {

    private Money balance;

    public AccountBalanceRequest(@JsonProperty(required = true,  value="accountId")
                                         Long accountId,
                                 @JsonProperty(required = true,  value="balance")
                                         Money balance) {
        super(accountId);
        this.balance = balance;
    }
}
