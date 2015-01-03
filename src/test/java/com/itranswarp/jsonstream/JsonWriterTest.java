package com.itranswarp.jsonstream;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.google.gson.GsonBuilder;
import com.itranswarp.jsonstream.annotation.JsonIgnore;

public class JsonWriterTest {

    static final float DELTA = 0.00001f;

    JsonWriter prepareJsonWriter() {
        return new JsonWriter();
    }

    <T> T loadByGson(Class<T> clazz, String jsonStr) {
        return new GsonBuilder().serializeNulls().create().fromJson(jsonStr, clazz);
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
    public void testWriteNestedArrayWithOutOfMaxDepth() throws Exception {
        JsonWriter jw = prepareJsonWriter();
        Object[] array1 = new Object[] { "nested", null };
        array1[1] = array1;
        jw.write(array1);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testWriteObjectOk() throws Exception {
        JsonWriter jw = prepareJsonWriter();
        Bean obj = new Bean();
        obj.setPassword("PASSWORD");
        jw.write(obj);
        String json = jw.toString();
        System.out.println(json);
        Bean loaded = loadByGson(Bean.class, json);
        assertEquals("0x0001", loaded.id);
        assertEquals(99, loaded.age);
        assertEquals(true, loaded.gender);
        assertEquals(123456789000L, loaded.created_at);
        assertEquals(12.5f, loaded.price, DELTA);
        assertEquals(1.5e20, loaded.space, DELTA);
        assertEquals("Bean", loaded.name);
        // should ignore:
        assertEquals("******", loaded.password);
        assertNull(loaded.description);
        assertNull(loaded.tags);
        List<Object> beans = loaded.beans;
        assertEquals(true, beans.get(1));
        assertNull(beans.get(2));
        assertArrayEquals(new Object[] { 1.0, 1.0, 2.0, 3.0 }, ((List<Object>) beans.get(3)).toArray());
        Map<String, Object> nested = (Map<String, Object>) beans.get(0);
        assertArrayEquals(new Object[] { 1.0, 2.0, 3.0, 4.0 }, ((List<Object>) nested.get("pages")).toArray());
        assertEquals(3.0, nested.get("current"));
        // verify sub:
        assertArrayEquals(new Long[] { 1L, 2L, 3L, 4L }, loaded.sub.pages);
        assertEquals(3L, loaded.sub.current);
    }

}

class Bean {
    String id = "0x0001";
    int age = 99;
    boolean gender = true;
    long created_at = 123456789000L;
    float price = 12.5f;
    double space = 1.5e20;
    String name = "Bean";
    String description = null;
    List<String> tags = null;
    List<Object> beans = Arrays.asList(new Nested(), true, null, new int[] { 1, 1, 2, 3 });
    Nested sub = new Nested();

    String password = "******";
    @JsonIgnore
    public String getPassword() {
        return this.password;
    }
    public void setPassword(String password) {
        this.password = password;
    }
}

class Nested {
    Long[] pages = { 1L, 2L, 3L, 4L };
    long current = 3;
}
