package com.itranswarp.jsonstream;

import java.util.Map;

/**
 * Interface used to convert a JSON object (Map<String, Object>) to Java object.
 * 
 * @author Michael Liao
 */
public interface ObjectHook {

    /**
     * Convert Map<String, Object> to Java bean.
     * 
     * @param path The path of the JSON.
     * @param map Key-value pairs.
     * @param clazz The expected Java class.
     * @param typeAdapters Registered type-adapters to convert String to any target type.
     * @return
     */
    Object toObject(String path, Map<String, Object> map, Class<?> clazz, TypeAdapters typeAdapters);

}
