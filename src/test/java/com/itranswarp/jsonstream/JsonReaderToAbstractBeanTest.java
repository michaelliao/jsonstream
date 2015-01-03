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

public class JsonReaderToAbstractBeanTest {

    JsonReader prepareJsonReader(String s) {
        return new JsonReader(new StringReader(s), null, null, null);
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
    public void testParseUseBeanObjectHook() throws Exception {
        String s = "{\"name\":\"Bob\", \"dogs\": ["
                + "   {\"type\": \"Husky\", \"name\": \"Haha\", \"age\": 3, \"clever\": 50 }, "
                + "   {\"type\": \"Bulldog\", \"name\": \"Blue\", \"age\": 2, \"weight\": 12 } "
                + " ] }";
        ObjectTypeFinder typeFinder = new ObjectTypeFinder() {
            @Override
            public Class<?> find(Class<?> clazz,
                    Map<String, Object> jsonObject) {
                if (AbstractDog.class.equals(clazz)) {
                    Object value = jsonObject.get("type");
                    if ("Husky".equals(value)) {
                        return Husky.class;
                    }
                    if ("Bulldog".equals(value)) {
                        return Bulldog.class;
                    }
                    throw new RuntimeException("Cannot detect object type for AbstractDog.");
                }
                return clazz;
            }};
        BeanObjectHook beanObjectHook = new BeanObjectHook(typeFinder);
        JsonReader js = new JsonBuilder().useObjectHook(beanObjectHook).createReader(s);
        PetOwner owner = js.parse(PetOwner.class);
        assertEquals("Bob", owner.name);
        assertEquals(2, owner.dogs.size());
        Husky husky = (Husky) owner.dogs.get(0);
        assertEquals("Haha", husky.name);
        assertEquals(3, husky.age);
        assertEquals(50, husky.clever);
        Bulldog bulldog = (Bulldog) owner.dogs.get(1);
        assertEquals("Blue", bulldog.name);
        assertEquals(2, bulldog.age);
        assertEquals(12, bulldog.weight);
    }

}

class PetOwner {
    String name;
    List<AbstractDog> dogs;
}

abstract class AbstractDog {
    String name;
    int age;
}

class Bulldog extends AbstractDog {
    int weight;
}

class Husky extends AbstractDog {
    int clever;
}
