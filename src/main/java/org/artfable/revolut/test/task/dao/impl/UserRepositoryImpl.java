package org.artfable.revolut.test.task.dao.impl;

import com.google.inject.Inject;
import org.artfable.revolut.test.task.config.Bean;
import org.artfable.revolut.test.task.dao.UserRepository;
import org.artfable.revolut.test.task.model.Account;
import org.artfable.revolut.test.task.model.Currency;
import org.artfable.revolut.test.task.model.User;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author artfable
 * 12.08.18
 */
@Bean
@Transactional
class UserRepositoryImpl implements UserRepository {

    private EntityManager entityManager;

    @Inject
    public UserRepositoryImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<User> getAllUsers() {
        return entityManager.createQuery(String.format("select user from %s user", User.class.getName())).getResultList();
    }

    @Override
    public User getUser(Long userId) {
        return entityManager.find(User.class, userId);
    }

    @Override
    public User createNewUser() {
        User user = new User();
        entityManager.persist(user);
        entityManager.flush();
        return user;
    }

    @Override
    public User save(User user) {
        entityManager.persist(user);
        entityManager.flush();
        return user;
    }

    @Override
    public boolean delete(Long id) {
        User user = entityManager.find(User.class, id);
        if (user != null) {
            Map<Currency, Account> accounts = new HashMap<>(user.getAccounts());
            accounts.forEach((currency, account) -> entityManager.remove(account));
            entityManager.flush();
            entityManager.remove(user);
            entityManager.flush();
            return true;
        }

        return false;
    }
}
