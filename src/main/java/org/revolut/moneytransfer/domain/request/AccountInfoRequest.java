package org.revolut.moneytransfer.domain.request;

import lombok.Data;
import org.revolut.moneytransfer.domain.User;

/**
 * Created by Sougata Bhattacharjee
 * On 16.09.18
 */
@Data
public class AccountInfoRequest extends AccountRequest {

    private Long accountId;
    private User accountHolder;
}
