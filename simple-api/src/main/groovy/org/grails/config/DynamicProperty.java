package org.grails.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface DynamicProperty {

    String name();

    String defaultValue();

	boolean requiresRestart() default false;

	boolean hiddenValue() default false;
}
