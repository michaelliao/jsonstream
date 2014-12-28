package com.itranswarp.jsonstream;

import java.util.Map;

/**
 * Called when a new JSON object is created.
 * 
 * @author Michael Liao
 */
public interface JsonObjectFactory {

    Map<String, Object> createJsonObject();

}
