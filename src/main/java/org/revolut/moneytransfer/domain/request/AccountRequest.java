package org.revolut.moneytransfer.domain.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Created by Sougata Bhattacharjee
 * On 16.09.18
 */
@Data
public class AccountRequest {

    private Long accountId;

    public AccountRequest(@JsonProperty(required = true,  value="accountId")
                          final Long accountId) {
        this.accountId = accountId;
    }
}
