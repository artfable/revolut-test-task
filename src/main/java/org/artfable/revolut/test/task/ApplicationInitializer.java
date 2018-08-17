package org.artfable.revolut.test.task;

import com.fasterxml.jackson.core.JsonParseException;
import com.google.inject.Guice;
import com.google.inject.Injector;
import liquibase.Contexts;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.apache.log4j.PropertyConfigurator;
import org.artfable.revolut.test.task.config.ApplicationInitializationException;
import org.artfable.revolut.test.task.config.ApplicationModule;
import org.artfable.revolut.test.task.config.dao.JpaModule;
import org.artfable.revolut.test.task.config.rs.RequestManagerFactory;
import org.artfable.revolut.test.task.config.rs.RestController;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.ExceptionHandler;

import javax.persistence.Entity;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Properties;

import static spark.Spark.*;

/**
 * @author artfable
 * 11.08.18
 */
public class ApplicationInitializer {

    private static final Logger logger = LoggerFactory.getLogger(ApplicationInitializer.class);

    public static void main(String[] args) {
        Reflections reflections = new Reflections(ApplicationInitializer.class.getPackage().getName());

        // configure spark
        PropertyConfigurator.configure(ApplicationInitializer.class.getClassLoader().getResourceAsStream("log4j.properties"));
        port(8080);
        ExceptionHandler<Exception> illegalArgumentHandler = (exception, request, response) -> {
            response.status(400);
            response.body(exception.getMessage());
        };
        exception(IllegalArgumentException.class, illegalArgumentHandler);
        exception(NumberFormatException.class, illegalArgumentHandler);
        exception(JsonParseException.class, illegalArgumentHandler);

        // hibernate
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

        RequestManagerFactory requestManagerFactory = new RequestManagerFactory(sessionFactory);

        // integrate REST resources with hibernate
        before((request, response) -> requestManagerFactory.create());
        afterAfter((request, response) -> requestManagerFactory.get().close()); // even in case of exception

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

        // configure DI
        Injector injector = Guice.createInjector(new ApplicationModule(reflections, requestManagerFactory), new JpaModule(() -> requestManagerFactory.get().getEntityManager()));

        reflections.getTypesAnnotatedWith(RestController.class).forEach(beanClass -> {
            if (beanClass.isInterface() || beanClass.isAnnotation() || beanClass.isEnum()) {
                throw new ApplicationInitializationException(beanClass.getName() + " can't be initialized");
            }

            logger.debug("Found REST controller: " + beanClass.getSimpleName());
            try {
                Method init = beanClass.getMethod("init", Injector.class);
                init.setAccessible(true);
                init.invoke(null, injector);
            } catch (ReflectiveOperationException e) {
                throw new ApplicationInitializationException("Can't initialise controller: " + beanClass.getSimpleName(), e);
            }
        });
    }
}
