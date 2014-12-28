package com.itranswarp.jsonstream;

import static org.junit.Assert.*;

import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

public class JsonStreamTest {

    static final double DELTA = 0.00000001;

    JsonStream prepareJsonStream(String s) {
        return new JsonStream(s);
    }

    @Test
    public void testParseSingleStringOk() throws Exception {
        assertEquals("", prepareJsonStream("\"\"").parse());
        assertEquals(" ", prepareJsonStream("\" \"").parse());
        assertEquals("A\'BC", prepareJsonStream("\"A\'BC\"").parse());
        assertEquals("\r\n\t\b\f\\\"/$", prepareJsonStream("\"\\r\\n\\t\\b\\f\\\\\\\"\\/$\"").parse());
        assertEquals("English中文", prepareJsonStream("\"English中文\"").parse());
        assertEquals("English中文", prepareJsonStream("\"English\\u4E2d\\u6587\"").parse());
    }

    @Test
    public void testParseSingleStringFailed() throws Exception {
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
                prepareJsonStream(s).parse();
                fail("Not caught ParseException: " + s);
            }
            catch (JsonParseException e) {
                // ok!
            }
        }
    }

    @Test
    public void testParseSingleBoolean() throws Exception {
        assertEquals(Boolean.TRUE, prepareJsonStream("true").parse());
        assertEquals(Boolean.TRUE, prepareJsonStream("\ttrue \r\n ").parse());
        assertEquals(Boolean.TRUE, prepareJsonStream(" \n \t true \t \n ").parse());

        assertEquals(Boolean.FALSE, prepareJsonStream("false").parse());
        assertEquals(Boolean.FALSE, prepareJsonStream(" false \r\n ").parse());
        assertEquals(Boolean.FALSE, prepareJsonStream("\r  \nfalse \t \r").parse());
    }

    @Test
    public void testParseSingleNull() throws Exception {
        assertNull(prepareJsonStream("null").parse());
        assertNull(prepareJsonStream(" null \r\n ").parse());
        assertNull(prepareJsonStream(" null \t  ").parse());
        assertNull(prepareJsonStream(" \n \t null \r  ").parse());
    }

    @Test
    public void testParseSingleLongOk() throws Exception {
        String[] tests = {
                "0", "00", "000", "-0", "-00",
                "1", "01", "-1", "-01",
                "100", "1020", "-100", "-1020",
                "999000999", "999000999000", "-999000999", "-999000999000",
                "9007199254740991", "-9007199254740991"
        };
        for (String s : tests) {
            assertEquals(Long.parseLong(s), ((Long) prepareJsonStream(s).parse()).longValue());
            assertEquals(Long.parseLong(s), ((Long) prepareJsonStream(s + " \r\t\n \n").parse()).longValue());
            assertEquals(Long.parseLong(s), ((Long) prepareJsonStream("\n  \t " + s).parse()).longValue());
            assertEquals(Long.parseLong(s), ((Long) prepareJsonStream(" \n  \t \r" + s + "\t \r \n\n").parse()).longValue());
        }
    }

    @Test
    public void testParseSingleLongFailed() throws Exception {
        String[] tests = {
                "0", "00", "000", "-0", "-00",
                "1", "01", "-1", "-01",
                "100", "1020", "-100", "-1020",
                "999000999", "999000999000", "-999000999", "-999000999000",
                "9007199254740991", "-9007199254740991"
        };
        String[] pres = {
                "", " ", "-", "+", "  }", "  ] ", "\n.", "e"
        };
        String[] ends = {
                "", " ", "-", "+", "  }", "  ] ", "\n.", "e"
        };
        for (String s : tests) {
            for (String pre : pres) {
                for (String end : ends) {
                    if ("".equals(pre.trim() + end.trim()) || "-".equals(pre.trim() + end.trim())) {
                        continue;
                    }
                    try {
                        prepareJsonStream(pre + s + end).parse();
                        fail("Not caught JsonParseException when parse: " + pre + s + end + ".");
                    }
                    catch (JsonParseException e) {
                        // ok
                    }
                }
            }
        }
    }

    @Test
    public void testParseSingleDouble() throws Exception {
        String[] tests = {
                "0.0", "00.0", "0.00", "-00.0", "-0.00",
                "0.12", "1.01", "-0.12", "-1.01",
                "12e0", "12e1", "1e01", "12E1", "1E01",
                "12e+0", "12e+1", "1e+01", "12E+1", "1E+01",
                "-12e-0","-12e1", "-1e01", "-12E1", "-1E01",
                "-12e+1", "-1e+01", "-12E+1", "-1E+01",
                "1.23e12", "1.23e-1", "1.23e-2", "123e-12", "1.23e-12",
                "-1.23e-1", "-1.23e-2", "-123e-12", "-1.23e-12",
                "-1.23e+1", "-1.23e+2", "-123e+12", "-1.23e+12"
        };
        for (String s : tests) {
            assertEquals(Double.parseDouble(s), ((Double) prepareJsonStream(s).parse()).doubleValue(), DELTA);
            assertEquals(Double.parseDouble(s), ((Double) prepareJsonStream(s + " \r\t\n \n").parse()).doubleValue(), DELTA);
            assertEquals(Double.parseDouble(s), ((Double) prepareJsonStream("\n  \t " + s).parse()).doubleValue(), DELTA);
            assertEquals(Double.parseDouble(s), ((Double) prepareJsonStream(" \n  \t \r" + s + "\t \r \n\n").parse()).doubleValue(), DELTA);
        }
    }

    @Test
    public void testParseSingleDoubleFailed() throws Exception {
        String[] tests = {
                "0.0", "00.0", "0.00", "-00.0", "-0.00",
                "0.12", "1.01", "-0.12", "-1.01",
                "12e0", "12e1", "1e01", "12E1", "1E01",
                "12e+0", "12e+1", "1e+01", "12E+1", "1E+01",
                "-12e-0","-12e1", "-1e01", "-12E1", "-1E01",
                "-12e+1", "-1e+01", "-12E+1", "-1E+01",
                "1.23e12", "1.23e-1", "1.23e-2", "123e-12", "1.23e-12",
                "-1.23e-1", "-1.23e-2", "-123e-12", "-1.23e-12",
                "-1.23e+1", "-1.23e+2", "-123e+12", "-1.23e+12"
        };
        String[] pres = {
                "", " ", "-", "+", "  }", "  ] ", "\n.", "e"
        };
        String[] ends = {
                "", " ", "-", "+", "  }", "  ] ", "\n.", "e"
        };
        for (String s : tests) {
            for (String pre : pres) {
                for (String end : ends) {
                    if ("".equals(pre.trim() + end.trim()) || "-".equals(pre.trim() + end.trim())) {
                        continue;
                    }
                    try {
                        prepareJsonStream(pre + s + end).parse();
                        fail("Not caught JsonParseException when parse: " + pre + s + end + ".");
                    }
                    catch (JsonParseException e) {
                        // ok
                    }
                }
            }
        }
    }

    @Test
    public void testParseEmptyArrayAndObjectOk() throws Exception {
        String[] tests = {
                "[]", "  []", "\n \r[] \t \n", "  \n  []\t \r\r \n",
                " [ ] ", " [    ] ", "\r \n[\n ]\t \t", " [  \n]\t ", " \n[ \t \n ]\t \n"
        };
        Object[] expecteds = {};
        for (String s : tests) {
            assertArrayEquals(expecteds, ((List<?>) prepareJsonStream(s).parse()).toArray());
            Map<?, ?> map = (Map<?, ?>) prepareJsonStream(s.replace('[', '{').replace(']', '}')).parse();
            assertTrue(map.isEmpty());
        }
    }

    @Test
    public void testParseNonEmptyArrayOk() throws Exception {
        String[] tests = {
                "[\"TEST\",true,1,false,2.5,null,\"END\"]",
                "[ \"TEST\", true, 1, false , 2.5 ,null, \"END\" ]",
                " [ \"TEST\", \ntrue, 1,\tfalse , 2.5 ,null, \"END\" ] ",
                "\n[\n  \"TEST\", true, 1, false , 2.5 ,null, \"END\"\r]\n\t",
                "\r[\t\"TEST\",\n \n \r true,    1,    false \n, 2.5 ,\tnull, \"END\"\n ]\t"
        };
        Object[] expecteds = {"TEST", true, 1L, false, 2.5, null, "END"};
        for (String s : tests) {
            assertArrayEquals(expecteds, ((List<?>) prepareJsonStream(s).parse()).toArray());
        }
    }

}
