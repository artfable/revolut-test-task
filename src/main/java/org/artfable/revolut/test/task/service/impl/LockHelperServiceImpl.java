package org.artfable.revolut.test.task.service.impl;

import com.google.inject.Inject;
import org.artfable.revolut.test.task.config.Bean;
import org.artfable.revolut.test.task.config.rs.RequestManager;
import org.artfable.revolut.test.task.model.Account;
import org.artfable.revolut.test.task.service.LockHelperService;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author artfable
 * 17.08.18
 */
@Bean
public class LockHelperServiceImpl implements LockHelperService {

    private RequestManager requestManager;

    @Inject
    public LockHelperServiceImpl(RequestManager requestManager) {
        this.requestManager = requestManager;
    }

    @Override
    public <T> T lockedOperation(Long accountId, Class<?> entityClass, Supplier<T> action) {
        String key = entityClass.getSimpleName() + accountId;
        requestManager.lock(key);

        try {
            return action.get();
        } finally {
            requestManager.releaseLock(key);
        }
    }

    @Override
    public <T> T lockedOperation(Supplier<T> action, Class<?> entityClass, Long... ids) {
        List<String> keys = Stream.of(ids)
                .map(id -> entityClass.getSimpleName() + id)
                .sorted() // sort is needed to avoid dead lock
                .collect(Collectors.toList());

        keys.forEach(requestManager::lock);

        try {
            return action.get();
        } finally {
            keys.forEach(requestManager::releaseLock);
        }

    }
}
