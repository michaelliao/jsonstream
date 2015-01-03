package com.itranswarp.jsonstream.validator.impl;

import org.junit.Before;
import org.junit.Test;

import com.itranswarp.jsonstream.annotation.EnumString;
import com.itranswarp.jsonstream.validator.impl.StringValidator;
import com.itranswarp.jsonstream.ValidateException;

public class EnumStringTest {

	StringValidator enumValidator;
	EnumStringBean bean;

	@Before
	public void setUp() throws Exception {
		enumValidator = new StringValidator(EnumStringBean.class.getDeclaredField("enumValue"));
		bean = new EnumStringBean();
	}

	@Test
	public void testContainsEnumValue() {
		enumValidator.validate(".org", "path", "value");
	}

	@Test(expected=ValidateException.class)
	public void testNotContainsEnumValue() {
		enumValidator.validate(".gov", "path", "value");
	}

	@Test
	public void testEnumValueIsNull() {
		enumValidator.validate(null, "path", "value");
	}

}

class EnumStringBean {

	@EnumString({ ".edu", ".com", ".net", ".org" })
	String enumValue;

}
