package com.itranswarp.jsonstream;

import java.util.List;

/**
 * Called when a new JSON array is created.
 * 
 * @author Michael Liao
 */
public interface JsonArrayFactory {

    List<Object> createJsonArray();

}
