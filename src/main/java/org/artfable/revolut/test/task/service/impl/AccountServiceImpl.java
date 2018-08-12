package org.artfable.revolut.test.task.service.impl;

import com.google.inject.Inject;
import org.artfable.revolut.test.task.config.Bean;
import org.artfable.revolut.test.task.dao.AccountRepository;
import org.artfable.revolut.test.task.dao.ExchangeRateRepository;
import org.artfable.revolut.test.task.dao.UserRepository;
import org.artfable.revolut.test.task.model.Account;
import org.artfable.revolut.test.task.model.Currency;
import org.artfable.revolut.test.task.model.ExchangeRate;
import org.artfable.revolut.test.task.model.User;
import org.artfable.revolut.test.task.service.AccountService;

import javax.persistence.NoResultException;
import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author artfable
 * 11.08.18
 */
@Bean
class AccountServiceImpl implements AccountService {

    private UserRepository userRepository;
    private AccountRepository accountRepository;
    private ExchangeRateRepository exchangeRateRepository;

    @Inject
    public AccountServiceImpl(UserRepository userRepository, AccountRepository accountRepository, ExchangeRateRepository exchangeRateRepository) {
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
        this.exchangeRateRepository = exchangeRateRepository;
    }

    @Override
    public Collection<Account> getAllAccountsByUser(Long userId) {
        Map<Currency, Account> accountsByUser = accountRepository.getAllAccountsByUser(userId);
        if (accountsByUser == null) {
            throw new IllegalArgumentException("Can't find any accounts data related to user [" + userId + "]");
        }
        return accountsByUser.values();
    }

    @Override
    public Account getAccount(Long accountId) {
        return accountRepository.getAccount(accountId);
    }

    @Override
    public synchronized Account openAccount(Long userId, Currency currency) {
        User user = userRepository.getUser(userId);
        if (user == null) {
            throw new IllegalArgumentException("User with id [" + userId + "] doesn't exist");
        }

        Map<Currency, Account> accounts = user.getAccounts();

        if (accounts.containsKey(currency)) {
            throw new IllegalArgumentException("Account for [" + currency + "] currency is already exist for user [" + userId + "]");
        }

        Account account = new Account(userId, currency);
        user.addAccount(account);
        accountRepository.save(account);
        userRepository.save(user);

        return account;
    }

    @Override
    public synchronized Account topUpAccount(Long accountId, double amount) {
        Account account = accountRepository.getAccount(accountId);
        if (account == null) {
            throw new IllegalArgumentException("Account doesn't exist");
        }

        return accountRepository.save(add(account, amount));
    }

    @Override
    public synchronized Account withdrawFromAccount(Long accountId, double amount) {
        Account account = accountRepository.getAccount(accountId);
        if (account == null) {
            throw new IllegalArgumentException("Account doesn't exist");
        }

        return accountRepository.save(subtract(account, amount));
    }

    @Override
    public synchronized boolean delete(Long accountId) {
        return accountRepository.delete(accountId);
    }

    @Override
    @Transactional
    public synchronized List<Account> transfer(Long fromAccountId, Long toAccountId, double amount) {
        List<Account> accounts = new ArrayList<>();

        Account fromAccount = accountRepository.getAccount(fromAccountId);
        if (fromAccount == null) {
            throw new IllegalArgumentException("Account with id [" + fromAccountId + "] doesn't exist");
        }

        Account toAccount = accountRepository.getAccount(toAccountId);
        if (toAccount == null) {
            throw new IllegalArgumentException("Account with id [" + toAccountId + "] doesn't exist");
        }

        accounts.add(subtract(fromAccount, amount));
        BigDecimal newAmount = toAccount.getAmount().add(convertAmount(fromAccount, toAccount, amount));
        toAccount.setAmount(newAmount);
        accounts.add(toAccount);

        return accountRepository.save(accounts);
    }

    private BigDecimal convertAmount(Account fromAccount, Account toAccount, double amount) {
        if (fromAccount.getCurrency() == toAccount.getCurrency()) {
            return BigDecimal.valueOf(amount).setScale(2, RoundingMode.FLOOR);
        }

        ExchangeRate exchangeRate;
        try {
            exchangeRate = exchangeRateRepository.getExchangeRateByCurrencies(fromAccount.getCurrency(), toAccount.getCurrency());
        } catch (NoResultException e) {
            throw new IllegalArgumentException("Exchange Rate for the pair [" + fromAccount.getCurrency() + " - " + toAccount.getCurrency() + "] isn't provided", e);
        }

        // exchangeRate can have 4 digits after point
        return BigDecimal.valueOf(amount).multiply(BigDecimal.valueOf(exchangeRate.getRate())).setScale(2, RoundingMode.FLOOR);
    }

    /**
     * Add amount to the account.
     * As we keep value with only 2 digits after point, we'll round value.
     *
     * @param account
     * @param amount
     * @return {@link Account}
     */
    private Account add(Account account, double amount) {
        BigDecimal newAmount = account.getAmount().add(BigDecimal.valueOf(amount).setScale(2, RoundingMode.FLOOR));
        account.setAmount(newAmount);
        return account;
    }

    /**
     * Subtract amount from the account if it possible.
     * In case of subtract round after subtraction is done.
     *
     * @param account
     * @param amount
     * @throws IllegalArgumentException if amount can't be subtracted
     * @return {@link Account}
     */
    private Account subtract(Account account, double amount) {
        BigDecimal newAmount = account.getAmount().subtract(BigDecimal.valueOf(amount)).setScale(2, RoundingMode.FLOOR);
        if (newAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Withdraw from account [" + account.getId() + "] isn't possible as there's not enough amount");
        }

        account.setAmount(newAmount);

        return account;
    }
}
