package org.revolut.moneytransfer.domain.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.revolut.moneytransfer.domain.AccountStatus;

/**
 * Created by Sougata Bhattacharjee
 * On 16.09.18
 */
@Data
public class AccountStatusRequest extends AccountRequest {

    @JsonProperty(required = true, value = "status")
    private AccountStatus status;

    public AccountStatusRequest(@JsonProperty(required = true, value = "accountId") final Long accountId,
                                @JsonProperty(required = true, value = "status") final AccountStatus status) {
        super(accountId);
        this.status = status;
    }
}
