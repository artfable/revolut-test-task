package org.artfable.revolut.test.task.model;

import javax.persistence.*;

/**
 * @author artfable
 * 12.08.18
 */
@Entity
@Table(name = "EXCHANGE_RATES")
public class ExchangeRate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    @Enumerated(EnumType.STRING)
    private Currency first;

    @Column
    @Enumerated(EnumType.STRING)
    private Currency second;

    @Column
    private Double rate;

    private ExchangeRate() {}

    public ExchangeRate(Currency first, Currency second, Double rate) {
        this.first = first;
        this.second = second;
        this.rate = rate;
    }

    public Currency getFirst() {
        return first;
    }

    public Currency getSecond() {
        return second;
    }

    public Double getRate() {
        return rate;
    }
}
