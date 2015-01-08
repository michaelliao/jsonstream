package com.itranswarp.jsonstream.validator.impl;

import org.junit.Before;
import org.junit.Test;

import com.itranswarp.jsonstream.annotation.MaximumInteger;
import com.itranswarp.jsonstream.annotation.MaximumNumber;
import com.itranswarp.jsonstream.annotation.MinimumInteger;
import com.itranswarp.jsonstream.annotation.MinimumNumber;
import com.itranswarp.jsonstream.validator.impl.IntegerValidator;
import com.itranswarp.jsonstream.validator.impl.NumberValidator;
import com.itranswarp.jsonstream.JsonValidateException;

public class MaxMinTest {

	IntegerValidator maxIntegerValidator;
	IntegerValidator minIntegerValidator;

	NumberValidator maxNumberValidator;
	NumberValidator minNumberValidator;

	MaxMinBean bean;

	@Before
	public void setUp() throws Exception {
		minIntegerValidator = new IntegerValidator(MaxMinBean.class.getDeclaredField("minInteger"));
		maxIntegerValidator = new IntegerValidator(MaxMinBean.class.getDeclaredField("maxInteger"));
		minNumberValidator = new NumberValidator(MaxMinBean.class.getDeclaredField("minNumber"));
		maxNumberValidator = new NumberValidator(MaxMinBean.class.getDeclaredField("maxNumber"));
		bean = new MaxMinBean();
	}

	@Test
	public void testGreaterThanMinInteger() {
		minIntegerValidator.validate(11L, "path", "value");
	}

	@Test
	public void testEqualToMinInteger() {
		minIntegerValidator.validate(10L, "path", "value");
	}

	@Test(expected=JsonValidateException.class)
	public void testLessThanMinInteger() {
		minIntegerValidator.validate(9L, "path", "value");
	}

	@Test
	public void testLessThanMaxInteger() {
		maxIntegerValidator.validate(99L, "path", "value");
	}

	@Test
	public void testEqualToMaxInteger() {
		maxIntegerValidator.validate(100L, "path", "value");
	}

	@Test(expected=JsonValidateException.class)
	public void testGreaterThanMaxInteger() {
		maxIntegerValidator.validate(101L, "path", "value");
	}

	@Test
	public void testGreaterThanMinNumber() {
		minNumberValidator.validate(10.6, "path", "value");
	}

	@Test
	public void testEqualToMinNumber() {
		minNumberValidator.validate(10.5, "path", "value");
	}

	@Test(expected=JsonValidateException.class)
	public void testLessThanMinNumber() {
		minNumberValidator.validate(10.4, "path", "value");
	}

	@Test
	public void testLessThanMaxNumber() {
		maxNumberValidator.validate(100.4, "path", "value");
	}

	@Test
	public void testEqualToMaxNumber() {
		maxNumberValidator.validate(100.5, "path", "value");
	}

	@Test(expected=JsonValidateException.class)
	public void testGreaterThanMaxNumber() {
		maxNumberValidator.validate(100.6, "path", "value");
	}

}

class MaxMinBean {

	@MinimumInteger(10)
	long minInteger;

	@MaximumInteger(100)
	long maxInteger;

	@MinimumNumber(10.5)
	double minNumber;

	@MaximumNumber(100.5)
	long maxNumber;

}
