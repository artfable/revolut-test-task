package org.artfable.revolut.test.task;

import com.fasterxml.jackson.core.JsonParseException;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.apache.log4j.PropertyConfigurator;
import org.artfable.revolut.test.task.config.ApplicationInitializationException;
import org.artfable.revolut.test.task.config.ApplicationModule;
import org.artfable.revolut.test.task.config.JpaModule;
import org.artfable.revolut.test.task.config.RestController;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.ExceptionHandler;

import static spark.Spark.exception;
import static spark.Spark.port;

/**
 * @author artfable
 * 11.08.18
 */
public class ApplicationInitializer {

    private static final Logger logger = LoggerFactory.getLogger(ApplicationInitializer.class);

    public static void main(String[] args) {
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

        // configure DI
        Reflections reflections = new Reflections(ApplicationInitializer.class.getPackage().getName());
        Injector injector = Guice.createInjector(new ApplicationModule(reflections), new JpaModule(reflections));

        reflections.getTypesAnnotatedWith(RestController.class).forEach(beanClass -> {
            if (beanClass.isInterface() || beanClass.isAnnotation() || beanClass.isEnum()) {
                throw new ApplicationInitializationException(beanClass.getName() + " can't be initialized");
            }

            logger.debug("Found REST controller: " + beanClass.getSimpleName());
            injector.getInstance(beanClass);
        });
    }
}
