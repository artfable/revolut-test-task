package org.artfable.revolut.test.task.service;

import java.util.function.Supplier;

/**
 * Helper service for reducing of a boilerplate code. Wrap with obtaining locks.
 *
 * @author artfable
 * 17.08.18
 */
public interface LockHelperService {
    <T> T lockedOperation(Long accountId, Class<?> entityClass, Supplier<T> action);

    <T> T lockedOperation(Supplier<T> action, Class<?> entityClass, Long... ids);
}
