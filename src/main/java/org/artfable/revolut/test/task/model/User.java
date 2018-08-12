package org.artfable.revolut.test.task.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * {@link User} is needed only for grouping accounts.
 *
 * @author artfable
 * 12.08.18
 */
@Entity
@Table(name = "USERS")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID")
    @MapKeyColumn(name = "CURRENCY")
    @MapKeyEnumerated(EnumType.STRING)
    @JsonIgnore
    private Map<Currency, Account> accounts = new HashMap<>();

    public Long getId() {
        return id;
    }

    public Map<Currency, Account> getAccounts() {
        return Collections.unmodifiableMap(accounts);
    }

    public void addAccount(Account account) {
        accounts.putIfAbsent(account.getCurrency(), account);
    }

    public Account removeAccount(Currency currency) {
        return accounts.remove(currency);
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                '}';
    }
}
