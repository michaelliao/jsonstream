package com.itranswarp.jsonstream;

import java.util.Map;

/**
 * Called when a new JSON object is created.
 * 
 * @author Michael Liao
 */
public interface JsonObjectFactory {

    /**
     * Called when a Json object is about to create. Expected a Map object.
     * 
     * @return Java Map object.
     */
    Map<String, Object> createJsonObject();

}
