package org.artfable.revolut.test.task.config;

import com.google.inject.AbstractModule;
import com.google.inject.matcher.Matchers;
import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.aopalliance.intercept.MethodInterceptor;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;
import org.reflections.Reflections;

import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.PersistenceException;
import javax.transaction.Transactional;
import java.io.IOException;
import java.util.Properties;

/**
 * {@link com.google.inject.Module} that configure db environment.
 * It creates {@link EntityManager} by hibernate and run liquibase for sql migration.
 *
 * @author artfable
 * 12.08.18
 */
public class JpaModule extends AbstractModule {
    private Reflections reflections;

    public JpaModule(Reflections reflections) {
        this.reflections = reflections;
    }

    @Override
    protected void configure() {
        Properties properties = new Properties();
        try {
            properties.load(JpaModule.class.getClassLoader().getResourceAsStream("hibernate.properties"));
        } catch (IOException e) {
            throw new ApplicationInitializationException("Failed to read hibernate.properties", e);
        }

        Configuration configuration = new Configuration();
        configuration.addProperties(properties);

        reflections.getTypesAnnotatedWith(Entity.class).forEach(entityClass -> {
            if (entityClass.isInterface() || entityClass.isAnnotation() || entityClass.isEnum()) {
                throw new ApplicationInitializationException(entityClass.getName() + " can't be initialized");
            }

            configuration.addAnnotatedClass(entityClass);
        });

        ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder().applySettings(configuration.getProperties()).build();
        SessionFactory sessionFactory = configuration.buildSessionFactory(serviceRegistry);
        EntityManager entityManager = sessionFactory.createEntityManager();

        bind(EntityManager.class).toProvider(() -> entityManager);

        MethodInterceptor transactionInterceptor = methodInvocation -> {
            Object result;

            EntityTransaction transaction = entityManager.getTransaction();
            if (!transaction.isActive()) {
                transaction.begin();
                try {
                    result = methodInvocation.proceed();
                } catch (Exception e) {
                    transaction.rollback();
                    entityManager.clear();
                    throw new PersistenceException(e);
                }
                transaction.commit();
                return result;
            }

            return methodInvocation.proceed();
        };
        bindInterceptor(Matchers.annotatedWith(Transactional.class), Matchers.any(), transactionInterceptor);
        bindInterceptor(Matchers.any(), Matchers.annotatedWith(Transactional.class), transactionInterceptor);

        // liquibase
        Session session = sessionFactory.openSession();
        session.doWork(connection -> {
            try {
                Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection));
                Liquibase liquibase = new Liquibase("changelog.xml", new ClassLoaderResourceAccessor(), database);
                liquibase.update(new Contexts());
                connection.commit();
            } catch (LiquibaseException e) {
                throw new ApplicationInitializationException("Liquibase failed to start", e);
            }
        });
        session.close();
    }
}
