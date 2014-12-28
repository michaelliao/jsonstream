package com.itranswarp.jsonstream;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;

public class JsonStreamBuilder {

    Reader reader;
    JsonObjectFactory jsonObjectFactory = null;
    JsonArrayFactory jsonArrayFactory = null;
    ObjectHook<?> objectHook = null;

    public JsonStreamBuilder(String jsonStr) {
        this.reader = new StringReader(jsonStr);
    }

    public JsonStreamBuilder(Reader reader) {
        this.reader = reader;
    }

    public JsonStreamBuilder(InputStream input) {
        try {
            this.reader = new InputStreamReader(input, "UTF-8");
        }
        catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public JsonStreamBuilder useJsonObjectFactory(JsonObjectFactory jsonObjectFactory) {
        this.jsonObjectFactory = jsonObjectFactory;
        return this;
    }

    public JsonStreamBuilder useJsonArrayFactory(JsonArrayFactory jsonArrayFactory) {
        this.jsonArrayFactory = jsonArrayFactory;
        return this;
    }

    public JsonStreamBuilder useObjectHook(ObjectHook<?> objectHook) {
        this.objectHook = objectHook;
        return this;
    }

    public JsonStream create() {
        return new JsonStream(reader, jsonObjectFactory, jsonArrayFactory, objectHook);
    }
}
