package com.itranswarp.jsonstream;

import java.util.Map;

/**
 * Object that implemented this interface will be serialize to JSON by itself.
 * 
 * @author Michael Liao
 */
public interface JsonSerializable {

    /**
     * Convert an object to JSON object as Map.
     * 
     * @return A Map object.
     */
    Map<String, Object> toJson();

}
