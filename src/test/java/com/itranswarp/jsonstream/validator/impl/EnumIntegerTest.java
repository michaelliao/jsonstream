package com.itranswarp.jsonstream.validator.impl;

import org.junit.Before;
import org.junit.Test;

import com.itranswarp.jsonstream.annotation.EnumInteger;
import com.itranswarp.jsonstream.JsonValidateException;
import com.itranswarp.jsonstream.validator.impl.IntegerValidator;

public class EnumIntegerTest {

	IntegerValidator enumValidator;
	EnumIntegerBean bean;

	@Before
	public void setUp() throws Exception {
		enumValidator = new IntegerValidator(EnumIntegerBean.class.getDeclaredField("enumValue"));
		bean = new EnumIntegerBean();
	}

	@Test
	public void testContainsEnumValue() {
		enumValidator.validate(35L, "path", "value");
	}

	@Test(expected=JsonValidateException.class)
	public void testNotContainsEnumValue() {
		enumValidator.validate(11L, "path", "value");
	}

	@Test
	public void testEnumValueIsNull() {
		enumValidator.validate(null, "path", "value");
	}

}

class EnumIntegerBean {

	@EnumInteger({ 15, 25, 35, 45 })
	Long enumValue;

}
