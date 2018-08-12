package org.artfable.revolut.test.task.dao;

import org.artfable.revolut.test.task.model.Currency;
import org.artfable.revolut.test.task.model.ExchangeRate;

import javax.persistence.NoResultException;

/**
 * Repository for exchange rates. To simplify the task possibility to change rates wasn't implemented.
 * All data will be provided by liquibase migration.
 *
 * @author artfable
 * 12.08.18
 */
public interface ExchangeRateRepository {

    /**
     *
     * @param first
     * @param second
     * @throws NoResultException if there is no result
     * @return
     */
    ExchangeRate getExchangeRateByCurrencies(Currency first, Currency second);
}
