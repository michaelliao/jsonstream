package com.itranswarp.jsonstream.validator.impl;

import org.junit.Before;
import org.junit.Test;

import com.itranswarp.jsonstream.annotation.Format;
import com.itranswarp.jsonstream.format.Email;
import com.itranswarp.jsonstream.format.LowerCase;
import com.itranswarp.jsonstream.validator.impl.StringValidator;
import com.itranswarp.jsonstream.ValidateException;

public class FormatStringTest {

	StringValidator formatValidator;
	FormatStringBean bean;

	@Before
	public void setUp() throws Exception {
		formatValidator = new StringValidator(FormatStringBean.class.getDeclaredField("emailValue"));
		bean = new FormatStringBean();
	}

    @Test
    public void testEmailIsOk() {
        formatValidator.validate("michael@itranswarp.com", "path", "value");
    }

	@Test(expected=ValidateException.class)
    public void testEmailIsOkButLowerCaseFailed() {
        formatValidator.validate("Michael@itranswarp.com", "path", "value");
    }

	@Test
	public void testEmailValueIsNull() {
	    formatValidator.validate(null, "path", "value");
	}

}

class FormatStringBean {

    @Format(Email.class)
	@Format(LowerCase.class)
	String emailValue;

}
