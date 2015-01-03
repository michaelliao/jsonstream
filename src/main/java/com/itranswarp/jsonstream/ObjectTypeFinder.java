package com.itranswarp.jsonstream;

import java.util.Map;

/**
 * Return a subclass by detect subclass type from the parsed JSON map 
 * when instantiate an abstract class.
 * 
 * @author Michael Liao
 */
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
