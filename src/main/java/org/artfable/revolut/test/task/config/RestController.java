package org.artfable.revolut.test.task.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for REST resources. Classes with this annotation will be initialised during startup of the application.
 *
 * @author artfable
 * 11.08.18
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface RestController {
}
