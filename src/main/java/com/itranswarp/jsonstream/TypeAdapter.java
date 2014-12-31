package com.itranswarp.jsonstream;

/**
 * Register a type adapter to convert between this type and string. TypeAdapter are only used to 
 * serialize or deserialize a JavaBean.
 * 
 * @author Michael Liao
 * 
 * @param <T> Type to serialize or deserialize to.
 */
public interface TypeAdapter<T> {

    /**
     * Convert string value to typed Java object.
     * 
     * @param s A string value.
     * @return A typed Java object.
     */
    T deserialize(String s);

    /**
     * Convert a Java object to a string.
     * 
     * @param t Java object.
     * @return String value.
     */
    String serialize(T t);
}
