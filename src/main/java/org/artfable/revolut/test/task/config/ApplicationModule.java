package org.artfable.revolut.test.task.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author artfable
 * 11.08.18
 */
public class ApplicationModule extends AbstractModule {

    private static final Logger logger = LoggerFactory.getLogger(ApplicationModule.class);

    private Reflections reflections;

    public ApplicationModule(Reflections reflections) {
        this.reflections = reflections;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void configure() {

        reflections.getTypesAnnotatedWith(Bean.class).forEach(beanClass -> {
            if (beanClass.isInterface() || beanClass.isAnnotation() || beanClass.isEnum()) {
                throw new ApplicationInitializationException(beanClass.getName() + " can't be initialized");
            }

            logger.debug("Found service: " + beanClass.getSimpleName());

            // For increasing scalability that beans should be RequestScope.
            // Synchronisation in services should be changed as well. Lock should be only on specific accounts/users.
            // It was missed to simplify the task as wasn't necessary.
            bind(beanClass).in(Singleton.class);
            for (Class interfaceClass : beanClass.getInterfaces()) {
                bind(interfaceClass).to(beanClass).in(Singleton.class);
            }
        });

        ObjectMapper objectMapper = new ObjectMapper();
        bind(ObjectMapper.class).toProvider(() -> objectMapper);
    }
}
