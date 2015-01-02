package com.itranswarp.jsonstream;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class PropertyUtils {

    /**
     * Get getter name. "getName" -> "name", "isMale" -> "male".
     * 
     * @param m Method object.
     * @return Property name of this getter.
     */
    private static String getGetterName(Method m) {
        String name = m.getName();
        if (name.startsWith("get") && (name.length() >= 4)
                && ! m.getReturnType().equals(void.class)
                && (m.getParameterTypes().length == 0)
        ) {
            return Character.toLowerCase(name.charAt(3)) + name.substring(4);
        }
        if (name.startsWith("is") && (name.length() >= 3)
                && (m.getReturnType().equals(boolean.class) || m.getReturnType().equals(Boolean.class))
                && (m.getParameterTypes().length == 0)
        ) {
            return Character.toLowerCase(name.charAt(2)) + name.substring(3);
        }
        return null;
    }

    /**
     * Get setter name. "setName" -> "name"
     * 
     * @param m Method object.
     * @return Property name of this setter.
     */
    private static String getSetterName(Method m) {
        String name = m.getName();
        if (name.startsWith("set") && (name.length() >= 4)
                && m.getReturnType().equals(void.class)
                && (m.getParameterTypes().length == 1)
        ) {
            return Character.toLowerCase(name.charAt(3)) + name.substring(4);
        }
        return null;
    }

    static Map<String, Method> getAllGetters(Class<?> clazz) {
        Map<String, Method> methods = new HashMap<String, Method>();
        while (clazz != null) {
            for (Method m : clazz.getDeclaredMethods()) {
                String propertyName = getGetterName(m);
                if (propertyName!=null && !methods.containsKey(propertyName)) {
                    methods.put(propertyName, m);
                }
            }
            clazz = clazz.getSuperclass();
        }
        return methods;
    }

    static Map<String, Method> getAllSetters(Class<?> clazz) {
        Map<String, Method> methods = new HashMap<String, Method>();
        while (clazz != null) {
            for (Method m : clazz.getDeclaredMethods()) {
                String propertyName = getSetterName(m);
                if (propertyName!=null && !methods.containsKey(propertyName)) {
                    methods.put(propertyName, m);
                }
            }
            clazz = clazz.getSuperclass();
        }
        return methods;
    }

    static Map<String, Field> getAllFields(Class<?> clazz) {
        Map<String, Field> fields = new HashMap<String, Field>();
        while (clazz != null) {
            for (Field f : clazz.getDeclaredFields()) {
                if (! fields.containsKey(f.getName())) {
                    fields.put(f.getName(), f);
                }
            }
            clazz = clazz.getSuperclass();
        }
        return fields;
    }

}
