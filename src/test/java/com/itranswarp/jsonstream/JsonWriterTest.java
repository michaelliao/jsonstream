package com.itranswarp.jsonstream;

import static org.junit.Assert.*;

import org.junit.Test;

public class JsonWriterTest {

    JsonWriter prepareJsonWriter() {
        return new JsonWriter();
    }

    @Test
    public void testWriteNull() throws Exception {
        JsonWriter jw = prepareJsonWriter();
        jw.write(null);
        assertEquals("null", jw.toString());
    }

    @Test
    public void testWriteInteger() throws Exception {
        JsonWriter jw = prepareJsonWriter();
        jw.write(12345);
        assertEquals("12345", jw.toString());
    }

    @Test
    public void testWriteFloat() throws Exception {
        JsonWriter jw = prepareJsonWriter();
        jw.write(123.456);
        assertEquals("123.456", jw.toString());
    }

    @Test
    public void testWriteE() throws Exception {
        JsonWriter jw = prepareJsonWriter();
        jw.write(1.2e56);
        assertEquals("1.2E56", jw.toString());
    }

    @Test
    public void testWriteString() throws Exception {
        JsonWriter jw = prepareJsonWriter();
        jw.write("Json\t\"STRING\"\r\nEND.");
        assertEquals("\"Json\\t\\\"STRING\\\"\\r\\nEND.\"", jw.toString());
    }

    @Test
    public void testWriteBooleanArray() throws Exception {
        JsonWriter jw = prepareJsonWriter();
        jw.write(new boolean[] {true, false, true, false});
        assertEquals("[true, false, true, false]", jw.toString());
    }

    @Test
    public void testWriteIntArray() throws Exception {
        JsonWriter jw = prepareJsonWriter();
        jw.write(new int[] {1, 2, 3, 4, 5});
        assertEquals("[1, 2, 3, 4, 5]", jw.toString());
    }

    @Test
    public void testWriteFloatArray() throws Exception {
        JsonWriter jw = prepareJsonWriter();
        jw.write(new float[] {1.0f, 2.5f, 0.5e10f});
        assertEquals("[1.0, 2.5, 5.0E9]", jw.toString());
    }

    @Test
    public void testWriteObjectArray() throws Exception {
        JsonWriter jw = prepareJsonWriter();
        jw.write(new Object[] {123, null,
                new int[] {0, -1, -2},
                true, 2.5, new short[0]});
        assertEquals("[123,null,[0, -1, -2],true,2.5,[]]", jw.toString());
    }

    @Test(expected=JsonSerializeException.class)
    public void testWriteNestedArray() throws Exception {
        JsonWriter jw = prepareJsonWriter();
        Object[] array1 = new Object[] { "nested", null };
        array1[1] = array1;
        jw.write(array1);
    }

}
