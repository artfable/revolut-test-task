package org.artfable.revolut.test.task.service.impl;

import com.google.inject.Inject;
import org.artfable.revolut.test.task.config.Bean;
import org.artfable.revolut.test.task.dao.UserRepository;
import org.artfable.revolut.test.task.model.User;
import org.artfable.revolut.test.task.service.UserService;

import java.util.List;

/**
 * @author artfable
 * 12.08.18
 */
@Bean
class UserServiceImpl implements UserService {

    private UserRepository userRepository;

    @Inject
    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
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
    public synchronized User createNewUser() {
        return userRepository.createNewUser();
    }

    @Override
    public synchronized boolean deleteUser(Long id) {
        return userRepository.delete(id);
    }
}
