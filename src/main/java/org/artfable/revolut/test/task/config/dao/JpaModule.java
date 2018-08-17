package org.artfable.revolut.test.task.config.dao;

import com.google.inject.AbstractModule;
import com.google.inject.Provider;
import com.google.inject.matcher.Matchers;
import org.aopalliance.intercept.MethodInterceptor;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.PersistenceException;
import javax.transaction.Transactional;

/**
 * {@link com.google.inject.Module} that configure injections and interceptions related to hibernate..
 *
 * @author artfable
 * 12.08.18
 */
public class JpaModule extends AbstractModule {
    private Provider<EntityManager> entityManagerProvider;

    public JpaModule(Provider<EntityManager> entityManagerProvider) {
        this.entityManagerProvider = entityManagerProvider;
    }

    @Override
    protected void configure() {
        bind(EntityManager.class).toProvider(entityManagerProvider);

        MethodInterceptor transactionInterceptor = methodInvocation -> {
            Object result;

            EntityTransaction transaction = entityManagerProvider.get().getTransaction();
            if (!transaction.isActive()) {
                transaction.begin();
                try {
                    result = methodInvocation.proceed();
                } catch (Exception e) {
                    transaction.rollback();
                    throw new PersistenceException(e);
                }
                transaction.commit();
                return result;
            }

            return methodInvocation.proceed();
        };
        bindInterceptor(Matchers.annotatedWith(Transactional.class), Matchers.any(), transactionInterceptor);
        bindInterceptor(Matchers.any(), Matchers.annotatedWith(Transactional.class), transactionInterceptor);
    }
}
