package org.artfable.revolut.test.task.config.rs;

import javax.persistence.EntityManager;
import java.io.Closeable;
import java.io.IOException;

/**
 * Context of a request. It helped to organise request scope.
 *
 * @author artfable
 * 16.08.18
 */
public interface RequestManager extends Closeable {

    /**
     * {@link EntityManager} for a request.
     *
     * @return
     */
    EntityManager getEntityManager();

    /**
     * Obtain lock on a provided key.
     *
     * @param key
     */
    void lock(String key);

    /**
     * Release lock from a proded key.
     *
     * @param key
     */
    void releaseLock(String key);
}
