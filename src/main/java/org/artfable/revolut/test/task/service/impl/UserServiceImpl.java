package org.artfable.revolut.test.task.service.impl;

import com.google.inject.Inject;
import org.artfable.revolut.test.task.config.Bean;
import org.artfable.revolut.test.task.dao.AccountRepository;
import org.artfable.revolut.test.task.dao.UserRepository;
import org.artfable.revolut.test.task.model.Account;
import org.artfable.revolut.test.task.model.User;
import org.artfable.revolut.test.task.service.LockHelperService;
import org.artfable.revolut.test.task.service.UserService;

import javax.transaction.Transactional;
import java.util.List;

/**
 * @author artfable
 * 12.08.18
 */
@Bean
class UserServiceImpl implements UserService {

    private UserRepository userRepository;
    private AccountRepository accountRepository;

    private LockHelperService lockHelperService;

    @Inject
    public UserServiceImpl(UserRepository userRepository, AccountRepository accountRepository, LockHelperService lockHelperService) {
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
        this.lockHelperService = lockHelperService;
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.getAllUsers();
    }

    @Override
    public User getUser(Long id) {
        return userRepository.getUser(id);
    }

    @Override
    public User createNewUser() {
        return userRepository.createNewUser();
    }

    @Override
    public boolean deleteUser(Long id) {
        return lockHelperService.lockedOperation(id, User.class, () -> deleteUserInTransaction(id));
    }

    @Transactional
    public boolean deleteUserInTransaction(Long id) { // should be public for the method interceptor
        User user = userRepository.getUser(id);

        if (user != null) {
            Long[] accountIds = user.getAccounts().values().stream().map(Account::getId).toArray(Long[]::new);
            lockHelperService.lockedOperation(() -> {
                for (Long accountId : accountIds) {
                    accountRepository.delete(accountId);
                }

                return null;
            }, Account.class, accountIds);
        }

        return userRepository.delete(id);
    }
}
