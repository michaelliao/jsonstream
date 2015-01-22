package com.itranswarp.jsonstream;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

/**
 * Builder for create JsonReader and JsonWriter much easier.
 * 
 * @author Michael Liao
 */
public class JsonBuilder {

    JsonObjectFactory jsonObjectFactory = null;
    JsonArrayFactory jsonArrayFactory = null;
    ObjectMapper objectMapper = null;
    TypeAdapters typeAdapters = new TypeAdapters();

    /**
     * Create a JsonBuilder with default options.
     */
    public JsonBuilder() {
    }

    /**
     * Register a TypeAdapter.
     * 
     * @param <T> Type to adapted-to.
     * @param clazz The adapted-to class.
     * @param typeAdapter The TypeAdapter instance.
     * @return JsonBuilder itself.
     */
    public <T> JsonBuilder registerTypeAdapter(Class<T> clazz, TypeAdapter<T> typeAdapter) {
        typeAdapters.registerTypeAdapter(clazz, typeAdapter);
        return this;
    }

    /**
     * Use JsonObjectFactory to create object when parse JSON.
     * 
     * @param jsonObjectFactory JsonObjectFactory instance.
     * @return JsonBuilder itself.
     */
    public JsonBuilder useJsonObjectFactory(JsonObjectFactory jsonObjectFactory) {
        this.jsonObjectFactory = jsonObjectFactory;
        return this;
    }

    /**
     * Use JsonArrayFactory to create array when parse JSON.
     * 
     * @param jsonArrayFactory JsonArrayFactory instance.
     * @return JsonBuilder itself.
     */
    public JsonBuilder useJsonArrayFactory(JsonArrayFactory jsonArrayFactory) {
        this.jsonArrayFactory = jsonArrayFactory;
        return this;
    }

    /**
     * Use ObjectMapper to create JavaBean from Map when parse JSON.
     * 
     * @param objectMapper ObjectMapper instance.
     * @return JsonBuilder itself.
     */
    public JsonBuilder useObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        return this;
    }

    /**
     * Create a JsonReader by providing a JSON string.
     * 
     * @param str The JSON string.
     * @return JsonReader object.
     */
    public JsonReader createReader(String str) {
        return createReader(new StringReader(str));
    }

    /**
     * Create a JsonReader by providing a Reader.
     * 
     * @param reader The Reader object.
     * @return JsonReader object.
     */
    public JsonReader createReader(Reader reader) {
        return new JsonReader(reader, jsonObjectFactory, jsonArrayFactory, objectMapper, typeAdapters);
    }

    /**
     * Create a JsonReader by providing an InputStream.
     * 
     * @param input The InputStream object.
     * @return JsonReader object.
     */
    public JsonReader createReader(InputStream input) {
        try {
            return createReader(new InputStreamReader(input, "UTF-8"));
        }
        catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Create a JsonWriter.
     * 
     * @return JsonWriter object.
     */
    public JsonWriter createWriter() {
        return new JsonWriter(typeAdapters);
    }

    /**
     * Create a JsonWriter that write JSON to specified Writer.
     * 
     * @param writer The Writer object.
     * @return JsonWriter object.
     */
    public JsonWriter createWriter(Writer writer) {
        return new JsonWriter(writer, typeAdapters);
    }

    /**
     * Create a JsonWriter that write JSON to specified OutputStream.
     * 
     * @param output The OutputStream object.
     * @return JsonWriter object.
     */
    public JsonWriter createWriter(OutputStream output) {
        Writer writer = null;
        try {
            writer = new OutputStreamWriter(output, "UTF-8");
        }
        catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        return new JsonWriter(writer, typeAdapters);
    }
}
