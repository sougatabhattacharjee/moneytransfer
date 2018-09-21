package org.revolut.moneytransfer.domain.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.revolut.moneytransfer.domain.User;

/**
 * Created by Sougata Bhattacharjee
 * On 16.09.18
 */
@Data
public class AccountInfoRequest extends AccountRequest {

    private User accountHolder;

    public AccountInfoRequest(@JsonProperty(required = true, value = "accountId") final Long accountId,
                              @JsonProperty(required = true, value = "accountHolder") final User accountHolder) {
        super(accountId);
        this.accountHolder = accountHolder;
    }
}
