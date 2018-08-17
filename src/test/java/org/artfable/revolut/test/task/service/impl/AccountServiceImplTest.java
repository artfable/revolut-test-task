package org.artfable.revolut.test.task.service.impl;

import org.artfable.revolut.test.task.dao.AccountRepository;
import org.artfable.revolut.test.task.dao.ExchangeRateRepository;
import org.artfable.revolut.test.task.model.Account;
import org.artfable.revolut.test.task.model.Currency;
import org.artfable.revolut.test.task.model.ExchangeRate;
import org.artfable.revolut.test.task.service.LockHelperService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.util.List;
import java.util.function.Supplier;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Mockito.when;

/**
 * @author artfable
 * 12.08.18
 */
@RunWith(MockitoJUnitRunner.class)
public class AccountServiceImplTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private ExchangeRateRepository exchangeRateRepository;

    @Mock
    private LockHelperService lockHelperService;

    @InjectMocks
    private AccountServiceImpl accountService;

    @Before
    public void setUp() {
        when(lockHelperService.lockedOperation(any(), any(), Matchers.<Long>anyVararg())).thenAnswer(invocation -> {
            Supplier action = invocation.getArgumentAt(0, Supplier.class);
            return action.get();
        });
    }

    @Test
    public void testTransfer() {
        long account1Id = 1l;
        long account2Id = 2l;

        Account account1 = new Account(account1Id, Currency.EUR);
        Account account2 = new Account(account2Id, Currency.EUR);

        account1.setAmount(BigDecimal.valueOf(100));

        when(accountService.getAccount(account1Id)).thenReturn(account1);
        when(accountService.getAccount(account2Id)).thenReturn(account2);
        when(accountRepository.save(anyListOf(Account.class))).thenAnswer(invocation -> invocation.getArguments()[0]);

        List<Account> accounts = accountService.transfer(account1Id, account2Id, 10.15);

        assertEquals(2, accounts.size());
        assertEquals(account1, accounts.get(0));
        assertEquals(account2, accounts.get(1));

        assertEquals(BigDecimal.valueOf(89.85), account1.getAmount());
        assertEquals(BigDecimal.valueOf(10.15), account2.getAmount());

    }

    @Test
    public void testTransferExchange() {
        long account1Id = 1l;
        long account2Id = 2l;

        Account account1 = new Account(account1Id, Currency.USD);
        Account account2 = new Account(account1Id, Currency.EUR);

        account1.setAmount(BigDecimal.valueOf(100));

        when(accountService.getAccount(account1Id)).thenReturn(account1);
        when(accountService.getAccount(account2Id)).thenReturn(account2);
        when(accountRepository.save(anyListOf(Account.class))).thenAnswer(invocation -> invocation.getArguments()[0]);

        when(exchangeRateRepository.getExchangeRateByCurrencies(Currency.USD, Currency.EUR)).thenReturn(new ExchangeRate(Currency.USD, Currency.EUR, 24.4));

        List<Account> accounts = accountService.transfer(account1Id, account2Id, 10.15);

        assertEquals(2, accounts.size());
        assertEquals(account1, accounts.get(0));
        assertEquals(account2, accounts.get(1));

        assertEquals(BigDecimal.valueOf(89.85), account1.getAmount());
        assertEquals(BigDecimal.valueOf(10.15).multiply(BigDecimal.valueOf(24.4)).setScale(2), account2.getAmount());

    }

    @Test(expected = IllegalArgumentException.class)
    public void testTransferFailed() {
        long account1Id = 1l;
        long account2Id = 2l;

        Account account1 = new Account(account1Id, Currency.EUR);
        Account account2 = new Account(account2Id, Currency.EUR);

        when(accountService.getAccount(account1Id)).thenReturn(account1);
        when(accountService.getAccount(account2Id)).thenReturn(account2);
        when(accountRepository.save(anyListOf(Account.class))).thenAnswer(invocation -> invocation.getArguments()[0]);

        accountService.transfer(account1Id, account2Id, 10.15);
    }
}