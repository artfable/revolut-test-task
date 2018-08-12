package org.artfable.revolut.test.task.dao.impl;

import com.google.inject.Inject;
import org.artfable.revolut.test.task.config.Bean;
import org.artfable.revolut.test.task.dao.ExchangeRateRepository;
import org.artfable.revolut.test.task.model.Currency;
import org.artfable.revolut.test.task.model.ExchangeRate;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;

/**
 * @author artfable
 * 12.08.18
 */
@Bean
@Transactional
public class ExchangeRateRepositoryImpl implements ExchangeRateRepository {

    private EntityManager entityManager;

    @Inject
    public ExchangeRateRepositoryImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public ExchangeRate getExchangeRateByCurrencies(Currency first, Currency second) {
        return (ExchangeRate) entityManager.createQuery(String.format("select rate from %s rate where rate.first = :first and rate.second = :second", ExchangeRate.class.getName()))
                .setParameter("first", first)
                .setParameter("second", second).getSingleResult();
    }
}
