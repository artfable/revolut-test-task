package org.artfable.revolut.test.task.model;

import javax.persistence.*;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

/**
 * @author artfable
 * 12.08.18
 */
@Entity
@Table(name = "ACCOUNTS")
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    @Enumerated(EnumType.STRING)
    private Currency currency;

    @Column(name = "USER_ID")
    private Long userId;

    @Column
    private BigDecimal amount = new BigDecimal(0);

    private Account() {}

    public Account(Long userId, Currency currency) {
        this.currency = currency;
        this.userId = userId;
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public Currency getCurrency() {
        return currency;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    @Override
    public String toString() {
        return "Account{" +
                "id=" + id +
                ", currency=" + currency +
                ", userId=" + userId +
                ", amount=" + amount +
                '}';
    }
}
