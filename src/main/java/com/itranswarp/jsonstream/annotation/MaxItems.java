package com.itranswarp.jsonstream.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The maximum items allowed for an array.
 * 
 * @author Michael Liao
 */
@Target({ ElementType.FIELD, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface MaxItems {

	int value() default Integer.MAX_VALUE;

}
