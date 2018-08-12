package org.artfable.revolut.test.task.dao.impl;

import com.google.inject.Inject;
import org.artfable.revolut.test.task.config.Bean;
import org.artfable.revolut.test.task.dao.AccountRepository;
import org.artfable.revolut.test.task.model.Account;
import org.artfable.revolut.test.task.model.Currency;
import org.artfable.revolut.test.task.model.User;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.util.List;
import java.util.Map;

/**
 * @author artfable
 * 12.08.18
 */
@Bean
@Transactional
class AccountRepositoryImpl implements AccountRepository {

    private EntityManager entityManager;

    @Inject
    public AccountRepositoryImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public Map<Currency, Account> getAllAccountsByUser(Long userId) {
        User user = entityManager.find(User.class, userId);
        return user != null ? user.getAccounts() : null;
    }

    @Override
    public Account getAccount(Long accountId) {
        return entityManager.find(Account.class, accountId);
    }

    @Override
    public Account save(Account account) {
        entityManager.persist(account);
        entityManager.flush();
        return account;
    }

    @Override
    public List<Account> save(List<Account> accounts) {
        accounts.forEach(account -> entityManager.persist(account));
        entityManager.flush();
        return accounts;
    }

    @Override
    public boolean delete(Long accountId) {
        Account account = entityManager.find(Account.class, accountId);
        if (account != null) {
            entityManager.remove(account);
            entityManager.flush();
            return true;
        }
        return false;
    }
}
