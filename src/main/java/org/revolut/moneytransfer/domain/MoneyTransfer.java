package org.revolut.moneytransfer.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.joda.time.DateTime;

import java.util.UUID;

/**
 * Created by Sougata Bhattacharjee
 * On 16.09.18
 */
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class MoneyTransfer {

    private String transferId;
    private long sourceAccountId;
    private long destinationAccountId;
    private Money amount;
    private DateTime transferDate;
    private String description;

    public MoneyTransfer() {
        this.transferId = UUID.randomUUID().toString();
    }

    public MoneyTransfer(final String transferId, final MoneyTransfer transfer) {
        this.transferId = transferId;
        this.sourceAccountId = transfer.getSourceAccountId();
        this.destinationAccountId = transfer.getDestinationAccountId();
        this.amount = transfer.getAmount();
        this.description = transfer.getDescription();
    }

    public MoneyTransfer(@JsonProperty(required = true, value = "sourceAccountId") final long sourceAccountId,
                         @JsonProperty(required = true, value = "destinationAccountId") final long destinationAccountId,
                         @JsonProperty(required = true, value = "amount") final Money amount,
                         @JsonProperty(required = true, value = "description") final String description) {
        this.sourceAccountId = sourceAccountId;
        this.destinationAccountId = destinationAccountId;
        this.amount = amount;
        this.description = description;
    }
}
