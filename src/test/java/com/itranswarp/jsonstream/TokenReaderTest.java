package com.itranswarp.jsonstream;

import static org.junit.Assert.*;

import java.io.StringReader;

import org.junit.Test;

public class TokenReaderTest {

    TokenReader prepareTokenReader(String s) {
        return new TokenReader(new CharReader(new StringReader(s)));
    }

    @Test
    public void testReadStringOk() throws Exception {
        assertEquals("", prepareTokenReader("\"\"").readString());
        assertEquals(" ", prepareTokenReader("\" \"").readString());
        assertEquals("A\'BC", prepareTokenReader("\"A\'BC\"").readString());
        assertEquals("\r\n\t\b\f\\\"/$", prepareTokenReader("\"\\r\\n\\t\\b\\f\\\\\\\"\\/$\"").readString());
        assertEquals("English中文", prepareTokenReader("\"English中文\"").readString());
        assertEquals("English中文", prepareTokenReader("\"English\\u4E2d\\u6587\"").readString());
    }

    @Test
    public void testReadStringFailed() throws Exception {
        String[] INVALID_STRINGS = {
                "\"abc\\\"def", // missing end "
                "\"abc \\a \"", // invalid \a
                "\"bb \n   \"", // invalid \n
                "\"bb \r   \"", // invalid \r
                "\" \\uF0Fp\"", // invalid unicode: F0Fp
                "\"  \\\'  \""  // invalid \'
        };
        for (String s : INVALID_STRINGS) {
            try {
                prepareTokenReader(s).readString();
                fail("Not caught ParseException: " + s);
            }
            catch (JsonParseException e) {
                // ok!
            }
        }
    }

    @Test
    public void testReadBooleanOk() throws Exception {
        assertTrue(prepareTokenReader("true").readBoolean());
        assertTrue(prepareTokenReader("true ").readBoolean());
        assertTrue(prepareTokenReader("true,").readBoolean());
        assertTrue(prepareTokenReader("true]").readBoolean());
        assertTrue(prepareTokenReader("true}").readBoolean());
        assertTrue(prepareTokenReader("true\t").readBoolean());
        assertTrue(prepareTokenReader("true\r").readBoolean());
        assertTrue(prepareTokenReader("true\n").readBoolean());
        assertFalse(prepareTokenReader("false").readBoolean());
        assertFalse(prepareTokenReader("false ").readBoolean());
        assertFalse(prepareTokenReader("false,").readBoolean());
        assertFalse(prepareTokenReader("false]").readBoolean());
        assertFalse(prepareTokenReader("false}").readBoolean());
        assertFalse(prepareTokenReader("false\t").readBoolean());
        assertFalse(prepareTokenReader("false\r").readBoolean());
        assertFalse(prepareTokenReader("false\n").readBoolean());
    }

    @Test
    public void testReadBooleanFailed() throws Exception {
        String[] INVALID_BOOLEANS = {
                "abc",
                "tABC",
                "truE",
                "TRUE",
                "tru\\",
                "fABC",
                "falsE",
                "False",
                "fals\\"
        };
        for (String s : INVALID_BOOLEANS) {
            try {
                prepareTokenReader(s).readBoolean();
                fail("Not caught ParseException: " + s);
            }
            catch (JsonParseException e) {
                // ok!
            }
        }
    }

    @Test
    public void testReadNullOk() throws Exception {
        prepareTokenReader("null").readNull();
        prepareTokenReader("null ").readNull();
        prepareTokenReader("null\t").readNull();
        prepareTokenReader("null\r").readNull();
        prepareTokenReader("null\n").readNull();
        prepareTokenReader("null,").readNull();
        prepareTokenReader("null]").readNull();
        prepareTokenReader("null}").readNull();
        prepareTokenReader("nulll").readNull();
    }

    @Test
    public void testReadNullFailed() throws Exception {
        String[] INVALID_NULLS = {
                "abc",
                "nUll",
                "nul",
                "nu",
                "nul1"
        };
        for (String s : INVALID_NULLS) {
            try {
                prepareTokenReader(s).readNull();
                fail("Not caught ParseException: " + s);
            }
            catch (JsonParseException e) {
                // ok!
            }
        }
    }

    @Test
    public void testString2LongOk() throws Exception {
        TokenReader reader = new TokenReader(new CharReader(new StringReader("")));
        assertEquals(0L, reader.string2Long("0", 0));
        assertEquals(1L, reader.string2Long("1", 0));
        assertEquals(12340090L, reader.string2Long("12340090", 0));
        assertEquals(123L, reader.string2Long("0123", 0));
        assertEquals(0L, reader.string2Long("000", 0));
        assertEquals(10000000000L, reader.string2Long("10000000000", 0));
        assertEquals(9007199254740991L, reader.string2Long("9007199254740991", 0));
    }

    @Test(expected = JsonParseException.class)
    public void testString2LongFailed() throws Exception {
        TokenReader reader = new TokenReader(new CharReader(new StringReader("")));
        reader.string2Long("9007199254740992", 0);
    }

    static final double DELTA = 0.00000001;

    @Test
    public void testString2FractionOk() throws Exception {
        TokenReader reader = new TokenReader(new CharReader(new StringReader("")));
        assertEquals(0.0, reader.string2Fraction("0", 0), DELTA);
        assertEquals(0.1, reader.string2Fraction("1", 0), DELTA);
        assertEquals(0.012, reader.string2Fraction("012", 0), DELTA);
        assertEquals(0.0505, reader.string2Fraction("0505", 0), DELTA);
        assertEquals(0.9991, reader.string2Fraction("99910", 0), DELTA);
    }

    @Test(expected = JsonParseException.class)
    public void testString2FractionFailed() throws Exception {
        TokenReader reader = new TokenReader(new CharReader(new StringReader("")));
        reader.string2Fraction("01234567891234567", 0);
    }

    @Test
    public void testReadNumberAsLongOk() throws Exception {
        String[] tests = {
                "0", "00", "000", "-0", "-00",
                "1", "01", "-1", "-01",
                "100", "1020", "-100", "-1020",
                "999000999", "999000999000", "-999000999", "-999000999000",
                "9007199254740991", "-9007199254740991"
        };
        for (String s : tests) {
            assertEquals(Long.parseLong(s), ((Long) prepareTokenReader(s).readNumber()).longValue());
            assertEquals(Long.parseLong(s), ((Long) prepareTokenReader(s + " ").readNumber()).longValue());
            assertEquals(Long.parseLong(s), ((Long) prepareTokenReader(s + ",").readNumber()).longValue());
            assertEquals(Long.parseLong(s), ((Long) prepareTokenReader(s + ";").readNumber()).longValue());
            assertEquals(Long.parseLong(s), ((Long) prepareTokenReader(s + "]").readNumber()).longValue());
            assertEquals(Long.parseLong(s), ((Long) prepareTokenReader(s + "}").readNumber()).longValue());
            assertEquals(Long.parseLong(s), ((Long) prepareTokenReader(s + "+").readNumber()).longValue());
            assertEquals(Long.parseLong(s), ((Long) prepareTokenReader(s + "\n").readNumber()).longValue());
            assertEquals(Long.parseLong(s), ((Long) prepareTokenReader(s + "\\").readNumber()).longValue());
            
        }
    }

    @Test
    public void testReadNumberAsDoubleOk() throws Exception {
        String[] tests = {
                "0.0", "00.0", "0.00", "-00.0", "-0.00", "0.12", "1.01", "-0.12", "-1.01", "102.035", "-102.035", "00102.003040500", "-00102.003040500"
        };
        String[] testsWithE = {
                "12e1", "1e01", "12E1", "1E01",
                "12e+1", "1e+01", "12E+1", "1E+01",
                "-12e1", "-1e01", "-12E1", "-1E01", "-12e+1", "-1e+01", "-12E+1", "-1E+01",
                "1.23e12", "1.23e-1", "1.23e-2", "123e-12", "1.23e-12",
                "-1.23e-1", "-1.23e-2", "-123e-12", "-1.23e-12",
                "-1.23e+1", "-1.23e+2", "-123e+12", "-1.23e+12"
        };
        for (String s : tests) {
            assertEquals(Double.parseDouble(s), ((Double) prepareTokenReader(s).readNumber()).doubleValue(), DELTA);
            assertEquals(Double.parseDouble(s), ((Double) prepareTokenReader(s + " ").readNumber()).doubleValue(), DELTA);
            assertEquals(Double.parseDouble(s), ((Double) prepareTokenReader(s + "a").readNumber()).doubleValue(), DELTA);
            assertEquals(Double.parseDouble(s), ((Double) prepareTokenReader(s + "-").readNumber()).doubleValue(), DELTA);
            assertEquals(Double.parseDouble(s), ((Double) prepareTokenReader(s + ".").readNumber()).doubleValue(), DELTA);
            assertEquals(Double.parseDouble(s), ((Double) prepareTokenReader(s + "\n").readNumber()).doubleValue(), DELTA);
            assertEquals(Double.parseDouble(s), ((Double) prepareTokenReader(s + ",").readNumber()).doubleValue(), DELTA);
            assertEquals(Double.parseDouble(s), ((Double) prepareTokenReader(s + "]").readNumber()).doubleValue(), DELTA);
            assertEquals(Double.parseDouble(s), ((Double) prepareTokenReader(s + "}").readNumber()).doubleValue(), DELTA);
        }
        for (String s : testsWithE) {
            assertEquals(Double.parseDouble(s), ((Double) prepareTokenReader(s).readNumber()).doubleValue(), DELTA);
            assertEquals(Double.parseDouble(s), ((Double) prepareTokenReader(s + " ").readNumber()).doubleValue(), DELTA);
            assertEquals(Double.parseDouble(s), ((Double) prepareTokenReader(s + "e").readNumber()).doubleValue(), DELTA);
            assertEquals(Double.parseDouble(s), ((Double) prepareTokenReader(s + "-").readNumber()).doubleValue(), DELTA);
            assertEquals(Double.parseDouble(s), ((Double) prepareTokenReader(s + ".").readNumber()).doubleValue(), DELTA);
            assertEquals(Double.parseDouble(s), ((Double) prepareTokenReader(s + "\n").readNumber()).doubleValue(), DELTA);
            assertEquals(Double.parseDouble(s), ((Double) prepareTokenReader(s + ",").readNumber()).doubleValue(), DELTA);
            assertEquals(Double.parseDouble(s), ((Double) prepareTokenReader(s + "]").readNumber()).doubleValue(), DELTA);
            assertEquals(Double.parseDouble(s), ((Double) prepareTokenReader(s + "}").readNumber()).doubleValue(), DELTA);
        }
    }

    @Test
    public void testReadNumberFailed() throws Exception {
        String[] INVALID_NUMBER_STRINGS = {
                "-",
                "+",
                "1.",
                "1.a",
                "-.5",
                "+1.5",
                "+123",
                "1.5e",
                "1.5e++1",
                "-1.5e++1",
                "-1.5e-a",
                "1.5E",
                "1.5E++1",
                "-1.5E++1",
                "-1.5E-a"
        };
        for (String s : INVALID_NUMBER_STRINGS) {
            try {
                prepareTokenReader(s).readNumber();
                fail("Not caught ParseException when parse: " + s);
            }
            catch (JsonParseException e) {
                // ok!
            }
        }
    }

}
