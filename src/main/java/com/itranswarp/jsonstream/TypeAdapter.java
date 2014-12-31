package com.itranswarp.jsonstream;

/**
 * Register a type adapter to convert between this type and string.
 * 
 * @author Michael Liao
 * 
 * @param <T> Type to serialize or deserialize to.
 */
public interface TypeAdapter<T> {

    T deserialize(String s);

    String serialize(T t);
}
