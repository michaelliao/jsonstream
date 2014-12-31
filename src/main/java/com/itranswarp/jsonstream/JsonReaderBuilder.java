package com.itranswarp.jsonstream;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;

/**
 * Builder for create JsonReader much easier.
 * 
 * @author Michael Liao
 */
public class JsonReaderBuilder {

    Reader reader;
    JsonObjectFactory jsonObjectFactory = null;
    JsonArrayFactory jsonArrayFactory = null;
    ObjectHook objectHook = null;

    public JsonReaderBuilder(String jsonStr) {
        this.reader = new StringReader(jsonStr);
    }

    public JsonReaderBuilder(Reader reader) {
        this.reader = reader;
    }

    public JsonReaderBuilder(InputStream input) {
        try {
            this.reader = new InputStreamReader(input, "UTF-8");
        }
        catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public JsonReaderBuilder useJsonObjectFactory(JsonObjectFactory jsonObjectFactory) {
        this.jsonObjectFactory = jsonObjectFactory;
        return this;
    }

    public JsonReaderBuilder useJsonArrayFactory(JsonArrayFactory jsonArrayFactory) {
        this.jsonArrayFactory = jsonArrayFactory;
        return this;
    }

    public JsonReaderBuilder useObjectHook(ObjectHook objectHook) {
        this.objectHook = objectHook;
        return this;
    }

    /**
     * Create a JsonReader object.
     * 
     * @return JsonReader object.
     */
    public JsonReader create() {
        return new JsonReader(reader, jsonObjectFactory, jsonArrayFactory, objectHook);
    }
}
