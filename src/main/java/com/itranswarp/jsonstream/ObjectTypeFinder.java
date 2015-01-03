package com.itranswarp.jsonstream;

import java.util.Map;

public interface ObjectTypeFinder {

    /**
     * Find a subclass for specified class. Must return a subclass or class itself.
     * 
     * @param clazz The expected type of object.
     * @param jsonObject The parsed JSON object as map.
     * @return Subclass or class itself.
     */
    Class<?> find(Class<?> clazz, Map<String, Object> jsonObject);
}
