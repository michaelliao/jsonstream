package com.itranswarp.jsonstream;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

public class JsonWriterWithJsonSerializableTest {

    String object2Json(Object obj) throws Exception {
        JsonWriter jsonWriter = new JsonBuilder().createWriter();
        jsonWriter.write(obj);
        return jsonWriter.toString();
    }

    @Test
    public void testWriteWithJsonSerializable() throws Exception {
        String s1 = object2Json(new Student1());
        String s2 = object2Json(new Student2());

        assertTrue(s1.indexOf("\"name\":\"Bob\"") > 0);
        assertTrue(s2.indexOf("\"name\":\"Bob\"") == (-1));

        assertTrue(s1.indexOf("\"score\":98") > 0);
        assertTrue(s2.indexOf("\"score\":98") == (-1));

        assertTrue(s2.indexOf("\"student\":\"Bob\"") > 0);
        assertTrue(s2.indexOf("\"grade\":\"A\"") > 0);
    }

}

class Student1 {
    String name = "Bob";
    int score = 98;
}

class Student2 implements JsonSerializable {
    String name = "Bob";
    int score = 98;

    @Override
    public Map<String, Object> toJson() {
        Map<String, Object> map = new HashMap<>();
        map.put("student", this.name);
        map.put("grade", getGrade());
        return map;
    }

    char getGrade() {
        if (score < 60) {
            return 'D';
        }
        if (score < 80) {
            return 'C';
        }
        if (score < 90) {
            return 'B';
        }
        return 'A';
    }
}
