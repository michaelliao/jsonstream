package com.itranswarp.jsonstream.validator.impl;

import java.lang.reflect.AnnotatedElement;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.itranswarp.jsonstream.ValidateException;
import com.itranswarp.jsonstream.annotation.EnumString;
import com.itranswarp.jsonstream.annotation.Format;
import com.itranswarp.jsonstream.annotation.MaxLength;
import com.itranswarp.jsonstream.annotation.MinLength;
import com.itranswarp.jsonstream.annotation.Pattern;
import com.itranswarp.jsonstream.annotation.Required;
import com.itranswarp.jsonstream.format.StringFormat;
import com.itranswarp.jsonstream.validator.Validator;

/**
 * A StringValidator accepts the following annotations:
 * 
 * <ul>
 *   <li>@Required</li>
 *   <li>@MinLength</li>
 *   <li>@MaxLength</li>
 *   <li>@Pattern</li>
 *   <li>@EnumString</li>
 *   <li>@Format</li>
 * </ul>
 * 
 * @author Michael Liao
 */
public class StringValidator implements Validator<String> {

	final boolean required;
	final Integer minLength;
	final Integer maxLength;
	final java.util.regex.Pattern pattern;
	final Set<String> enums;
	final StringFormat[] formats;

    public StringValidator(AnnotatedElement ae) {
        required = ae.isAnnotationPresent(Required.class);
        minLength = ae.isAnnotationPresent(MinLength.class)
                ? ae.getAnnotation(MinLength.class).value() : null;
        maxLength = ae.isAnnotationPresent(MaxLength.class)
                ? ae.getAnnotation(MaxLength.class).value() : null;
        pattern = ae.isAnnotationPresent(Pattern.class)
                ? java.util.regex.Pattern.compile(ae.getAnnotation(Pattern.class).value()) : null;
        enums = ae.isAnnotationPresent(EnumString.class)
                ? new HashSet<String>(Arrays.asList(ae.getAnnotation(EnumString.class).value())) : null;
        Format[] formatAnnos = ae.getAnnotationsByType(Format.class);
        formats = new StringFormat[formatAnnos.length];
        for (int i=0; i<formatAnnos.length; i++) {
            formats[i] = newStringFormat(formatAnnos[i].value());
        }
    }

	StringFormat newStringFormat(Class<? extends StringFormat> clazz) {
	    try {
            return clazz.newInstance();
        }
        catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
	}

	public void validate(String obj, String path, String name) {
		String fullpath = path + "." + name;
		if (required && obj == null) {
			throw new ValidateException("Required", fullpath);
		}
		if (obj == null) {
			return;
		}
		if ((minLength != null) && (obj.length() < minLength)) {
			throw new ValidateException("MinLength", fullpath);
		}
		if ((maxLength != null) && (obj.length() > maxLength)) {
			throw new ValidateException("MaxLength", fullpath);
		}
		if ((pattern != null) && !pattern.matcher(obj).matches()) {
			throw new ValidateException("Pattern", fullpath);
		}
		if (enums != null && !enums.contains(obj)) {
			throw new ValidateException("Enum", fullpath);
		}
		for (StringFormat sf : formats) {
		    if (! sf.validate(obj)) {
		        throw new ValidateException("Format", fullpath);
		    }
		}
	}

}
