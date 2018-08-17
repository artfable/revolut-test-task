package org.artfable.revolut.test.task.config.rs;

import com.google.inject.Provider;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

/**
 * @author artfable
 * 16.08.18
 */
public class RequestManagerFactory implements Provider<RequestManager> {

    private static final Logger logger = LoggerFactory.getLogger(RequestManagerFactory.class);

    private final Map<Long, RequestManagerImpl> requestManagerHolder = new ConcurrentHashMap<>();
    private SessionFactory sessionFactory;

    private final Map<String, CountDownLatch> innerLock = new ConcurrentHashMap<>();

    public RequestManagerFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    /**
     * Create a new {@link RequestManager} for a current request.
     *
     * @return
     */
    public RequestManager create() {
        long threadId = Thread.currentThread().getId();
        RequestManagerImpl requestManager = new RequestManagerImpl(threadId, sessionFactory);
        requestManagerHolder.put(threadId, requestManager);
        return requestManager;
    }

    /**
     * Get {@link RequestManager} for a current request.
     *
     * @return instance for a current request or null if wasn't created.
     */
    @Override
    public RequestManager get() {
        long threadId = Thread.currentThread().getId();
        return requestManagerHolder.get(threadId);
    }

    public class RequestManagerImpl implements RequestManager {

        private final long id;
        private final EntityManager entityManager;

        private RequestManagerImpl(long id, SessionFactory sessionFactory) {
            this.id = id;
            this.entityManager = sessionFactory.createEntityManager();

            logger.debug("RequestManager [" + id + "] was created");
        }

        @Override
        public EntityManager getEntityManager() {
            return entityManager;
        }

        @Override
        public void lock(String key) {
            CountDownLatch countDownLatch = new CountDownLatch(1);

            CountDownLatch existedCountDownLatch = innerLock.computeIfAbsent(key, s -> countDownLatch);
            while (existedCountDownLatch != countDownLatch) {
                try {
                    existedCountDownLatch.await();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                existedCountDownLatch = innerLock.computeIfAbsent(key, s -> countDownLatch);
            }
        }

        @Override
        public void releaseLock(String key) {
            innerLock.remove(key).countDown();
        }

        @Override
        public void close() {
            entityManager.close();
            requestManagerHolder.remove(Thread.currentThread().getId()); // can do that because the requestManager is unique per thread

            logger.debug("RequestManager [" + id + "] was closed");
        }
    }
}
