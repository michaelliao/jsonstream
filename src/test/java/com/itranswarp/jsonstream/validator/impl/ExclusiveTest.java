package com.itranswarp.jsonstream.validator.impl;

import org.junit.Before;
import org.junit.Test;

import com.itranswarp.jsonstream.annotation.ExclusiveMaximum;
import com.itranswarp.jsonstream.annotation.ExclusiveMinimum;
import com.itranswarp.jsonstream.annotation.MaximumInteger;
import com.itranswarp.jsonstream.annotation.MinimumInteger;
import com.itranswarp.jsonstream.JsonValidateException;

public class ExclusiveTest {

    IntegerValidator minValidator;
    IntegerValidator maxValidator;
    ExclusiveBean bean;

    @Before
    public void setUp() throws Exception {
        minValidator = new IntegerValidator(ExclusiveBean.class.getDeclaredField("minValue"));
        maxValidator = new IntegerValidator(ExclusiveBean.class.getDeclaredField("maxValue"));
        bean = new ExclusiveBean();
    }

    @Test
    public void testGreaterThanMin() {
        minValidator.validate(11L, "path", "value");
    }

    @Test(expected = JsonValidateException.class)
    public void testEqualToMin() {
        minValidator.validate(10L, "path", "value");
    }

    @Test(expected = JsonValidateException.class)
    public void testLessThanMin() {
        minValidator.validate(9L, "path", "value");
    }

    @Test
    public void testLessThanMax() {
        maxValidator.validate(99L, "path", "value");
    }

    @Test(expected = JsonValidateException.class)
    public void testEqualToMax() {
        maxValidator.validate(100L, "path", "value");
    }

    @Test(expected = JsonValidateException.class)
    public void testGreaterThanMax() {
        maxValidator.validate(101L, "path", "value");
    }

}

class ExclusiveBean {

    @MinimumInteger(10)
    @ExclusiveMinimum
    long minValue;

    @MaximumInteger(100)
    @ExclusiveMaximum
    long maxValue;

}
