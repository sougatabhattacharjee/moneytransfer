package org.revolut.moneytransfer.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
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

    @JsonCreator
    public Money(@JsonProperty(required = true,  value="amount")
                     final BigDecimal amount,
                 @JsonProperty(required = true,  value="currency") final Currency currency) {
        this.amount = amount;
        this.currency = currency;
    }


}
