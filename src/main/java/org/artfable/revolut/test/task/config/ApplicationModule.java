package org.artfable.revolut.test.task.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.AbstractModule;
import com.google.inject.Provider;
import org.artfable.revolut.test.task.config.rs.RequestManager;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link com.google.inject.Module} that configure base infrastructure.
 *
 * @author artfable
 * 11.08.18
 */
public class ApplicationModule extends AbstractModule {

    private static final Logger logger = LoggerFactory.getLogger(ApplicationModule.class);

    private Reflections reflections;
    private Provider<RequestManager> requestManagerProvider;

    public ApplicationModule(Reflections reflections, Provider<RequestManager> requestManagerProvider) {
        this.reflections = reflections;
        this.requestManagerProvider = requestManagerProvider;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void configure() {

        reflections.getTypesAnnotatedWith(Bean.class).forEach(beanClass -> {
            if (beanClass.isInterface() || beanClass.isAnnotation() || beanClass.isEnum()) {
                throw new ApplicationInitializationException(beanClass.getName() + " can't be initialized");
            }

            logger.debug("Found service: " + beanClass.getSimpleName());

            for (Class interfaceClass : beanClass.getInterfaces()) {
                bind(interfaceClass).to(beanClass);
            }
        });

        bind(RequestManager.class).toProvider(requestManagerProvider);

        ObjectMapper objectMapper = new ObjectMapper();
        bind(ObjectMapper.class).toProvider(() -> objectMapper);
    }
}
