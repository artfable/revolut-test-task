package org.artfable.revolut.test.task.dao;

import org.artfable.revolut.test.task.model.User;

import java.util.List;

/**
 * @author artfable
 * 12.08.18
 */
public interface UserRepository {

    List<User> getAllUsers();

    User getUser(Long userId);

    User createNewUser();

    User save(User user);

    boolean delete(Long id);
}
