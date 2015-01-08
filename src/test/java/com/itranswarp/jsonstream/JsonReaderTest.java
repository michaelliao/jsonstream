package com.itranswarp.jsonstream;

import static org.junit.Assert.*;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.google.gson.GsonBuilder;
import com.itranswarp.jsonstream.annotation.JsonIgnore;

public class JsonReaderTest {

    static final double DELTA = 0.00000001;

    JsonReader prepareJsonReader(String s) {
        return new JsonReader(new StringReader(s), null, null, null, null);
    }

    String prepareStandardJson(Object obj) {
        return new GsonBuilder().serializeNulls().create().toJson(obj);
    }

    Map<String, Object> prepareOrderedMap(Object ... args) {
        if (args.length % 2 != 0) {
            throw new RuntimeException("Must be key-value pairs.");
        }
        String key = null;
        Map<String, Object> map = new LinkedHashMap<String, Object>();
        for (Object o : args) {
            if (key == null) {
                key = (String) o;
            }
            else {
                map.put(key, o);
                key = null;
            }
        }
        return map;
    }

    List<Object> prepareList(Object ... args) {
        List<Object> list = new ArrayList<Object>();
        for (Object o : args) {
            list.add(o);
        }
        return list;
    }

    @Test
    public void testParseSingleStringOk() throws Exception {
        assertEquals("", prepareJsonReader("\"\"").parse());
        assertEquals(" ", prepareJsonReader("\" \"").parse());
        assertEquals("A\'BC", prepareJsonReader("\"A\'BC\"").parse());
        assertEquals("\r\n\t\b\f\\\"/$", prepareJsonReader("\"\\r\\n\\t\\b\\f\\\\\\\"\\/$\"").parse());
        assertEquals("English中文", prepareJsonReader("\"English中文\"").parse());
        assertEquals("English中文", prepareJsonReader("\"English\\u4E2d\\u6587\"").parse());
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
                prepareJsonReader(s).parse();
                fail("Not caught ParseException: " + s);
            }
            catch (JsonParseException e) {
                // ok!
            }
        }
    }

    @Test
    public void testParseSingleBoolean() throws Exception {
        assertEquals(Boolean.TRUE, prepareJsonReader("true").parse());
        assertEquals(Boolean.TRUE, prepareJsonReader("\ttrue \r\n ").parse());
        assertEquals(Boolean.TRUE, prepareJsonReader(" \n \t true \t \n ").parse());

        assertEquals(Boolean.FALSE, prepareJsonReader("false").parse());
        assertEquals(Boolean.FALSE, prepareJsonReader(" false \r\n ").parse());
        assertEquals(Boolean.FALSE, prepareJsonReader("\r  \nfalse \t \r").parse());
    }

    @Test
    public void testParseSingleNull() throws Exception {
        assertNull(prepareJsonReader("null").parse());
        assertNull(prepareJsonReader(" null \r\n ").parse());
        assertNull(prepareJsonReader(" null \t  ").parse());
        assertNull(prepareJsonReader(" \n \t null \r  ").parse());
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
            assertEquals(Long.parseLong(s), ((Long) prepareJsonReader(s).parse()).longValue());
            assertEquals(Long.parseLong(s), ((Long) prepareJsonReader(s + " \r\t\n \n").parse()).longValue());
            assertEquals(Long.parseLong(s), ((Long) prepareJsonReader("\n  \t " + s).parse()).longValue());
            assertEquals(Long.parseLong(s), ((Long) prepareJsonReader(" \n  \t \r" + s + "\t \r \n\n").parse()).longValue());
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
                        prepareJsonReader(pre + s + end).parse();
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
            assertEquals(Double.parseDouble(s), ((Double) prepareJsonReader(s).parse()).doubleValue(), DELTA);
            assertEquals(Double.parseDouble(s), ((Double) prepareJsonReader(s + " \r\t\n \n").parse()).doubleValue(), DELTA);
            assertEquals(Double.parseDouble(s), ((Double) prepareJsonReader("\n  \t " + s).parse()).doubleValue(), DELTA);
            assertEquals(Double.parseDouble(s), ((Double) prepareJsonReader(" \n  \t \r" + s + "\t \r \n\n").parse()).doubleValue(), DELTA);
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
                        prepareJsonReader(pre + s + end).parse();
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
            assertArrayEquals(expecteds, ((List<?>) prepareJsonReader(s).parse()).toArray());
            Map<?, ?> map = (Map<?, ?>) prepareJsonReader(s.replace('[', '{').replace(']', '}')).parse();
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
            assertArrayEquals(expecteds, ((List<?>) prepareJsonReader(s).parse()).toArray());
        }
    }

    @Test
    public void testParseNonEmptyObjectOk() throws Exception {
        String[] tests = {
                "{\"TEST\":true,\" num \":1,\"B\":false,\"--float--\":2.5,\"null\":null,\"\":\"END\"}",
                "{ \"TEST\": true,\" num \": 1, \"B\" : false ,\"--float--\"\t: 2.5 , \"null\"  : null, \"\":\t \t\"END\" }",
                " { \"TEST\": \ntrue, \" num \"\r:\n1,\t\"B\"\n:false , \"--float--\": 2.5 ,\n\"null\"\n:null, \"\"\r:\"END\" } ",
                "\n{\n  \"TEST\": true,\t\" num \":\t 1,\"B\"\n:\n false ,\n\n\"--float--\"\n:\n2.5 ,\"null\"\n:\nnull, \"\"\n:\n\"END\"\r}\n\t",
                "\r{\t\"TEST\":\n \n \r true,    \n\" num \":1,  \"B\":  false \n, \"--float--\"\r \r:\r2.5 ,\t\"null\" :null ,\n\"\": \"END\"\n }\t"
        };
        Object[] expecteds = {"TEST", " num ", "B", "--float--", "null", ""};
        Arrays.sort(expecteds);
        for (String s : tests) {
            @SuppressWarnings("unchecked")
            Map<String, Object> map = (Map<String, Object>) prepareJsonReader(s).parse();
            Object[] keys = map.keySet().toArray();
            Arrays.sort(keys);
            assertArrayEquals(expecteds, keys);
            // check key-value:
            assertTrue((Boolean) map.get("TEST"));
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testParseComplexObjectOk() throws Exception {
        Map<String, Object> map = prepareOrderedMap(
                "key1", true,
                "key2", null,
                "key3", prepareOrderedMap("sub1", 1234, "sub2", "SUB2", "sub3", false),
                "key4", "-END-");
        String src = prepareStandardJson(map);
        Map<String, Object> parsed = (Map<String, Object>) prepareJsonReader(src).parse();
        // check:
        assertTrue((Boolean) parsed.get("key1"));
        assertNull(parsed.get("key2"));
        assertEquals("-END-", parsed.get("key4"));
        Map<String, Object> nested = (Map<String, Object>) parsed.get("key3");
        assertEquals(1234L, ((Long) nested.get("sub1")).longValue());
        assertEquals("SUB2", nested.get("sub2"));
        assertFalse((Boolean) nested.get("sub3"));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testParseComplexObjectWithArrayOk() throws Exception {
        Map<String, Object> map = prepareOrderedMap(
                "key1", true,
                "key2", prepareList(12, 34.5, null, "LIST", false),
                "key3", prepareOrderedMap(
                        "sub1", prepareList(),
                        "sub2", "SUB2",
                        "sub3", prepareList(true)),
                "key4", "-END-");
        String src = prepareStandardJson(map);
        Map<String, Object> parsed = (Map<String, Object>) prepareJsonReader(src).parse();
        // check:
        assertTrue((Boolean) parsed.get("key1"));
        assertArrayEquals(new Object[] { 12L, 34.5, null, "LIST", false }, ((List<Object>) parsed.get("key2")).toArray());
        assertEquals("-END-", parsed.get("key4"));
        Map<String, Object> nested = (Map<String, Object>) parsed.get("key3");
        assertArrayEquals(new Object[] {}, ((List<Object>) nested.get("sub1")).toArray());
        assertEquals("SUB2", nested.get("sub2"));
        assertArrayEquals(new Object[] { true }, ((List<Object>) nested.get("sub3")).toArray());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testNestedArrayWithObjectOk() throws Exception {
        List<Object> list = prepareList(
                prepareList(
                        1,
                        2,
                        prepareList(
                                3,
                                4,
                                prepareList(prepareList()))),
                prepareList(
                        prepareList(
                                prepareList(5, 6))),
                prepareList(
                        prepareOrderedMap(),
                        prepareOrderedMap("array", prepareList(7, 8)),
                        prepareOrderedMap("array", prepareList(9, prepareList()))));
        String src = prepareStandardJson(list);
        List<Object> parsed = (List<Object>) prepareJsonReader(src).parse();
        assertEquals(src, prepareStandardJson(parsed));
    }

    @Test()
    public void testParseWithInvalidJson() throws Exception {
        String[] tests = {
                " { \"A\": [  \"missing ,\"   ] \"B\": 0 }",
                " [ { \"A\":    [ true, true ],  \"B\":  [ null, null, [ \"should be ] but }\" ] ], \"C\":   {}  }  }  ",
                " { \"A\": [  \"missing end }\"   ]  ",
                " { \"A\": [  \"missing ,\"   ] \"B\": 0 }",
                " { \"A\": [  \"missing ,\"  true ]  }",
                " { \"A\": [  \"missing ,\"  1.2 ]  }",
                " { \"A\": [  \"missing ,\"  null ]  }",
                " { \"A\": [  \"should be } but ]\"   ] ] ",
                " { \"A\": [ \"has extra }\", []] } }   "
        };
        for (String s : tests) {
            try {
                prepareJsonReader(s).parse();
                fail("Not caught JsonParseException when parse: " + s);
            }
            catch (JsonParseException e) {
                // ok, and log error message:
                System.out.println("Parse: " + s);
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    @Test
    public void testParseReturnGenericType() throws Exception {
        assertTrue(prepareJsonReader("true").parse(Boolean.class));
        assertNull(prepareJsonReader("null").parse(Object.class));
        assertEquals(12345L, prepareJsonReader("12345").parse(Long.class).longValue());
        assertEquals(123.456, prepareJsonReader("123.456").parse(Double.class).doubleValue(), DELTA);
        assertArrayEquals(new Object[] { 1L, 2L, 3L }, prepareJsonReader("[1,2,3]").parse(List.class).toArray());
        assertEquals("world", prepareJsonReader("{\"hello\":\"world\"}").parse(Map.class).get("hello"));
    }

    @Test
    public void testParseUseBeanObjectHook() throws Exception {
        String s = "{\"name\":\"Java\", \"avoidMe\": 999, \"shouldIgnore\": \"no\", \"version\":1.8, \"draft\":false, \"level\": 9, \"role\":\"TEACHER\", "
                + " \"longList\": [10, 20, 30, 40, 50],  "
                + " \"rawList\": [true, null, 100, [], \"RAW\"],  "
                + " \"longArray\": [1, 2, 3, 4, 5],  "
                + " \"intArray\": [-1, -2, -3, -4, -5],  "
                + " \"stringArray\": [null, \"@@@\"],  "
                + " \"friends\": [ { \"id\": 123, \"name\": \"A1\" }, null, { \"id\": 456, \"name\": \"A2\" }  ],  "
                + " \"address\":{ \"street\": \"No.1 West Road\", \"zipcode\": \"100101\"} }";
        JsonReader js = new JsonBuilder().createReader(s);
        User bean = js.parse(User.class);
        assertEquals(100L, User.avoidMe);
        assertEquals("yes", bean.shouldIgnore);
        assertTrue(bean.methodSetVersionIsCalled);
        assertEquals("Java", bean.name);
        assertEquals(9, bean.level);
        assertEquals(1.8, bean.version, DELTA);
        assertEquals(Role.TEACHER, bean.role);
        assertFalse(bean.draft);
        assertTrue(bean.address instanceof Address);
        assertEquals("No.1 West Road", bean.address.street);
        assertEquals("100101", bean.address.zipcode);
        System.out.println("User -> JSON: " + prepareStandardJson(bean));
    }

    @Test
    public void testParseUseCustomObjectHook() throws Exception {
        ObjectMapper objectHook = (path, map, clazz, adapters) -> {
            System.out.println("objectHook -> " + clazz);
            User bean = new User();
            bean.name = (String) map.get("name");
            bean.version = (double) map.get("version");
            bean.draft = (boolean) map.get("draft");
            bean.address = null;
            return bean;
        };
        String s = "{\"name\":\"Java\", \"version\":1.8, \"draft\":false, \"address\":{ \"street\": \"No.1 West Road\", \"zipcode\": \"100101\"} }";
        JsonReader js = new JsonBuilder().useObjectHook(objectHook).createReader(s);
        User bean = js.parse(User.class);
        assertEquals("Java", bean.name);
        assertEquals(1.8, bean.version, DELTA);
        assertFalse(bean.draft);
        assertNull(bean.address);
    }

}

abstract class AbstractUser {
    double version;
    boolean draft;

    boolean methodSetVersionIsCalled = false;

    void setVersion(double version) {
        System.out.println("call: setVersion(" + version + ");");
        this.version = version;
        this.methodSetVersionIsCalled = true;
    }
}

class User extends AbstractUser {
    static long avoidMe = 100L;
    String name;
    short level;
    String shouldIgnore = "yes";
    @JsonIgnore
    public void setShouldIgnore(String shouldIgnore) {
        this.shouldIgnore = shouldIgnore;
    }
    Address address;
    long[] longArray;
    int[] intArray;
    Role role;
    String[] stringArray;
    Collection<Long> longList;
    List<Friend> friends;
    @SuppressWarnings("rawtypes")
    List rawList;
}

class Address {
    String street;
    String zipcode;
}

class Friend {
    long id;
    String name;
}

enum Role {
    ADMIN,
    TEACHER,
    STUDENT
}
