package org.artfable.revolut.test.task.service;

import org.artfable.revolut.test.task.model.User;

import java.util.List;

/**
 * @author artfable
 * 12.08.18
 */
public interface UserService {

    List<User> getAllUsers();

    User getUser(Long id);

    User createNewUser();

    /**
     * Delete a {@link User} with a provided id.
     * @param id
     * @return false if nothing was done. true if the {@link User} was deleted successfully.
     */
    boolean deleteUser(Long id);
}
