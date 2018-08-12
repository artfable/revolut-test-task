package org.artfable.revolut.test.task.dao;

import org.artfable.revolut.test.task.model.Account;
import org.artfable.revolut.test.task.model.Currency;

import java.util.List;
import java.util.Map;

/**
 * @author artfable
 * 12.08.18
 */
public interface AccountRepository {
    Map<Currency, Account> getAllAccountsByUser(Long userId);

    Account getAccount(Long accountId);

    Account save(Account account);

    List<Account> save(List<Account> accounts);

    boolean delete(Long accountId);
}
