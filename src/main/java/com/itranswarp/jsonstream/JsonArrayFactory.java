package com.itranswarp.jsonstream;

import java.util.List;

/**
 * Called when a new JSON array is about to create.
 * 
 * @author Michael Liao
 */
public interface JsonArrayFactory {

    /**
     * Called when a Json object is about to create. Expected a List object.
     * 
     * @return A Java List object.
     */
    List<Object> createJsonArray();

}
