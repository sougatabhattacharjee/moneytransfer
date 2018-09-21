package org.revolut.moneytransfer.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Created by Sougata Bhattacharjee
 * On 15.09.18
 */
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class User {

    private String name;

    public User() {}

    public User(final String name) {
        this.name = name;
    }
}
