package com.itranswarp.jsonstream.validator.impl;

import org.junit.Before;
import org.junit.Test;

import com.itranswarp.jsonstream.annotation.Required;
import com.itranswarp.jsonstream.JsonValidateException;

public class RequiredTest {

    IntegerValidator integerValidator;
    NumberValidator numberValidator;
    StringValidator stringValidator;
    RequiredBean bean;

    @Before
    public void setUp() throws Exception {
        integerValidator = new IntegerValidator(RequiredBean.class.getDeclaredField("requiredLong"));
        numberValidator = new NumberValidator(RequiredBean.class.getDeclaredField("notRequiredDouble"));
        stringValidator = new StringValidator(RequiredBean.class.getDeclaredField("requiredString"));
        bean = new RequiredBean();
    }

    @Test
    public void testRequiredLong() {
        integerValidator.validate(1L, "path", "value");
    }

    @Test(expected = JsonValidateException.class)
    public void testRequiredLongButNull() {
        integerValidator.validate(null, "path", "value");
    }

    @Test
    public void testNotRequiredDouble() {
        numberValidator.validate(1.23, "path", "value");
    }

    @Test
    public void testNotRequiredDoubleAndNull() {
        numberValidator.validate(null, "path", "value");
    }

    @Test
    public void testRequiredString() {
        stringValidator.validate("", "path", "value");
    }

    @Test(expected = JsonValidateException.class)
    public void testRequiredStringButNull() {
        stringValidator.validate(null, "path", "value");
    }

}

class RequiredBean {

    @Required
    Long requiredLong;

    Double notRequiredDouble;

    @Required
    String requiredString;
}
