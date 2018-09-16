package org.revolut.moneytransfer.domain.request;

import lombok.Data;
import org.revolut.moneytransfer.domain.AccountStatus;

/**
 * Created by Sougata Bhattacharjee
 * On 16.09.18
 */
@Data
public class AccountStatusRequest extends AccountRequest {

    private Long accountId;
    private AccountStatus status;
}
