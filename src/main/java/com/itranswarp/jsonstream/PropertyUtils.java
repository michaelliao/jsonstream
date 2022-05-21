package com.itranswarp.jsonstream;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

import com.itranswarp.jsonstream.annotation.JsonIgnore;

class PropertyUtils {

    /**
     * Get getter name. "getName" -> "name", "isMale" -> "male".
     * 
     * @param m Method object.
     * @return Property name of this getter.
     */
    private static String getGetterName(Method m) {
        String name = m.getName();
        if (name.startsWith("get") && (name.length() >= 4) && !m.getReturnType().equals(void.class) && (m.getParameterTypes().length == 0)) {
            return Character.toLowerCase(name.charAt(3)) + name.substring(4);
        }
        if (name.startsWith("is") && (name.length() >= 3) && (m.getReturnType().equals(boolean.class) || m.getReturnType().equals(Boolean.class))
                && (m.getParameterTypes().length == 0)) {
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
        if (name.startsWith("set") && (name.length() >= 4) && m.getReturnType().equals(void.class) && (m.getParameterTypes().length == 1)) {
            return Character.toLowerCase(name.charAt(3)) + name.substring(4);
        }
        return null;
    }

    /**
     * Get all getters of this class, includes inherited methods, but excludes
     * static methods. Methods marked as @JsonIgnore are put to as null.
     * 
     * @param clazz
     * @return
     */
    static Map<String, Method> getAllGetters(Class<?> clazz) {
        Map<String, Method> methods = new HashMap<>();
        while (!clazz.equals(Object.class)) {
            for (Method m : clazz.getDeclaredMethods()) {
                if (Modifier.isStatic(m.getModifiers())) {
                    continue;
                }
                String propertyName = getGetterName(m);
                if (shouldIgnore(m)) {
                    methods.put(propertyName, null);
                }
                if (propertyName != null && !methods.containsKey(propertyName)) {
                    methods.put(propertyName, m);
                }
            }
            clazz = clazz.getSuperclass();
        }
        return methods;
    }

    /**
     * Get all setters of this class, includes inherited methods, but excludes
     * static methods. Methods marked as @JsonIgnore are put to as null.
     * 
     * @param clazz
     * @return
     */
    static Map<String, Method> getAllSetters(Class<?> clazz) {
        Map<String, Method> methods = new HashMap<>();
        while (!clazz.equals(Object.class)) {
            for (Method m : clazz.getDeclaredMethods()) {
                if (Modifier.isStatic(m.getModifiers())) {
                    continue;
                }
                String propertyName = getSetterName(m);
                if (shouldIgnore(m)) {
                    methods.put(propertyName, null);
                }
                if (propertyName != null && !methods.containsKey(propertyName)) {
                    methods.put(propertyName, m);
                }
            }
            clazz = clazz.getSuperclass();
        }
        return methods;
    }

    static Map<String, Field> getAllFields(Class<?> clazz) {
        Map<String, Field> fields = new HashMap<>();
        while (!clazz.equals(Object.class)) {
            for (Field f : clazz.getDeclaredFields()) {
                if (Modifier.isStatic(f.getModifiers()) || shouldIgnore(f)) {
                    continue;
                }
                if (!fields.containsKey(f.getName())) {
                    fields.put(f.getName(), f);
                }
            }
            clazz = clazz.getSuperclass();
        }
        return fields;
    }

    static boolean shouldIgnore(Method m) {
        return m.isAnnotationPresent(JsonIgnore.class);
    }

    static boolean shouldIgnore(Field f) {
        return f.isAnnotationPresent(JsonIgnore.class);
    }
}
