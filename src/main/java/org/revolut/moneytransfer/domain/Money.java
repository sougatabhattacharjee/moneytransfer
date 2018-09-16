package org.revolut.moneytransfer.domain;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;

/**
 * Created by Sougata Bhattacharjee
 * On 14.09.18
 */
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class Money {

    private BigDecimal amount;
    private Currency currency;

    public Money() {
        // to make Jackson happy
    }

    public Money(final BigDecimal amount, final Currency currency) {
        this.amount = amount;
        this.currency = currency;
    }


}
