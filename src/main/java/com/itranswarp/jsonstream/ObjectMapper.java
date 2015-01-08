package com.itranswarp.jsonstream;

import java.util.Map;

/**
 * Interface used to convert a JSON object {@code Map<String, Object>} to Java object.
 * 
 * @author Michael Liao
 */
public interface ObjectMapper {

    /**
     * Convert {@code Map<String, Object>} to Java bean.
     * 
     * @param path The path of the JSON.
     * @param map Key-value pairs.
     * @param clazz The expected Java class.
     * @param typeAdapters Registered type-adapters to convert String to any target type.
     * @return Java object.
     */
    Object toObject(String path, Map<String, Object> map, Class<?> clazz, TypeAdapters typeAdapters);

}
