package org.artfable.revolut.test.task.service;

import org.artfable.revolut.test.task.model.Account;
import org.artfable.revolut.test.task.model.Currency;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * @author artfable
 * 11.08.18
 */
public interface AccountService {
    Collection<Account> getAllAccountsByUser(Long userId);

    Account getAccount(Long accountId);

    /**
     * Open {@link Account} in provided {@link Currency} for {@link org.artfable.revolut.test.task.model.User}.
     * Only one account in the same {@link Currency} can be opened per {@link org.artfable.revolut.test.task.model.User}.
     *
     * @param userId
     * @param currency
     * @return new {@link Account}
     */
    Account openAccount(Long userId, Currency currency);

    /**
     * Add amount of money to an account.
     *
     * @param accountId
     * @param amount
     * @return
     */
    Account topUpAccount(Long accountId, double amount);

    /**
     * Subtract amount of money from an account.
     *
     * @param accountId
     * @param amount
     * @throws IllegalArgumentException in case if not enouch money
     * @return
     */
    Account withdrawFromAccount(Long accountId, double amount);

    /**
     * Delete an {@link Account} with a provided id.
     *
     * @param accountId
     * @return false if nothing was done. true if the {@link Account} was deleted successfully.
     */
    boolean delete(Long accountId);

    /**
     * Transfer money between accounts.
     *
     * @param fromAccountId
     * @param toAccountId
     * @param amount
     * @throws IllegalArgumentException in case if not enouch money
     * @return
     */
    List<Account> transfer(Long fromAccountId, Long toAccountId, double amount);
}
