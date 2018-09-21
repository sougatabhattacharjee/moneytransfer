package org.revolut.moneytransfer.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.joda.time.DateTime;

/**
 * Created by Sougata Bhattacharjee
 * On 14.09.18
 */

@Getter
@Setter
@EqualsAndHashCode
@ToString
public class Account {

    private long accountId;
    private User accountHolder;
    private Money balance;
    private AccountStatus status;
    private DateTime created;

    public Account() {
        // to make Jackson happy
        this.created = DateTime.now();
        this.status = AccountStatus.ACTIVE;
    }

    public Account(final long accountId, final Account account) {
        this.accountId = accountId;
        this.balance = account.getBalance();
        this.accountHolder = account.getAccountHolder();
        this.created = DateTime.now();
        this.status = AccountStatus.ACTIVE;
    }

    public Account(
            @JsonProperty(required = true, value = "accountHolder") final User accountHolder,
            @JsonProperty(required = true, value = "balance") final Money balance) {
        this.accountHolder = accountHolder;
        this.balance = balance;
        this.created = DateTime.now();
        this.status = AccountStatus.ACTIVE;
    }
}
