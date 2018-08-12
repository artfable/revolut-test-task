package org.artfable.revolut.test.task.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Classes annotated by this annotation will be resolved by {@link ApplicationModule}. Allow to inject through interface.
 *
 * @author artfable
 * 11.08.18
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Bean {
}
