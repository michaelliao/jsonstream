package com.itranswarp.jsonstream.validator.impl;

import java.lang.reflect.AnnotatedElement;
import java.util.HashSet;
import java.util.Set;

import com.itranswarp.jsonstream.ValidateException;
import com.itranswarp.jsonstream.annotation.EnumInteger;
import com.itranswarp.jsonstream.annotation.ExclusiveMaximum;
import com.itranswarp.jsonstream.annotation.ExclusiveMinimum;
import com.itranswarp.jsonstream.annotation.MaximumInteger;
import com.itranswarp.jsonstream.annotation.MinimumInteger;
import com.itranswarp.jsonstream.annotation.MultipleOf;
import com.itranswarp.jsonstream.annotation.Required;
import com.itranswarp.jsonstream.validator.Validator;

/**
 * An IntegerValidator accepts the following annotations:
 * 
 * <ul>
 *   <li>@Required</li>
 *   <li>@MultipleOf</li>
 *   <li>@MinimumInteger</li>
 *   <li>@MaximumInteger</li>
 *   <li>@ExclusiveMinimum</li>
 *   <li>@ExclusiveMaximum</li>
 *   <li>@EnumInteger</li>
 * </ul>
 * 
 * @author Michael Liao
 */
public class IntegerValidator implements Validator<Long> {

	final boolean required;
	final Long multipleOf;
	final Long minimum;
	final Long maximum;
	final boolean exclusiveMinimum;
	final boolean exclusiveMaximum;
	final Set<Long> enums;

	public IntegerValidator(AnnotatedElement ae) {
		required = ae.isAnnotationPresent(Required.class);
		multipleOf = ae.isAnnotationPresent(MultipleOf.class)
		        ? ae.getAnnotation(MultipleOf.class).value() : null;
		minimum = ae.isAnnotationPresent(MinimumInteger.class)
		        ? ae.getAnnotation(MinimumInteger.class).value() : null;
		maximum = ae.isAnnotationPresent(MaximumInteger.class)
		        ? ae.getAnnotation(MaximumInteger.class).value() : null;
		exclusiveMinimum = ae.isAnnotationPresent(ExclusiveMinimum.class);
		exclusiveMaximum = ae.isAnnotationPresent(ExclusiveMaximum.class);
		if (ae.isAnnotationPresent(EnumInteger.class)) {
			enums = new HashSet<Long>();
			for (long en : ae.getAnnotation(EnumInteger.class).value()) {
				enums.add(en);
			}
		}
		else {
			enums = null;
		}
	}

	public void validate(Long obj, String path, String name) {
		String fullpath = path + "." + name;
		if (required && obj == null) {
			throw new ValidateException("Required", fullpath);
		}
		if (obj == null) {
			return;
		}
		long value = obj.longValue();
		if ((multipleOf != null) && (value % multipleOf > 0)) {
			throw new ValidateException("MultipleOf", fullpath);
		}
		if (minimum != null) {
			if (value < minimum) {
				throw new ValidateException("Minimum", fullpath);
			}
			if (exclusiveMinimum && (value == minimum)) {
				throw new ValidateException("ExclusiveMinimum", fullpath);
			}
		}
		if (maximum != null) {
			if (value > maximum) {
				throw new ValidateException("Maximum", fullpath);
			}
			if (exclusiveMaximum && (value == maximum)) {
				throw new ValidateException("ExclusiveMaximum", fullpath);
			}
		}
		if (enums != null && !enums.contains(obj)) {
			throw new ValidateException("Enum", fullpath);
		}
	}

}
