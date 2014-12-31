package com.itranswarp.jsonstream;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

class PropertySetters {

    static final Field[] EMPTY_FIELDS = new Field[0];
    static final Method[] EMPTY_METHODS = new Method[0];

    // final Log log = LogFactory.getLog(getClass());
    final Class<?> clazz;
    final Map<String, PropertySetter> map;

    public PropertySetters(Class<?> clazz) {
        this.clazz = clazz;
        Map<String, PropertySetter> map = new HashMap<String, PropertySetter>();
        Map<String, Method> methods = getAllMethods(clazz);
        for (String propertyName : methods.keySet()) {
            Method m = methods.get(propertyName);
            if (propertyName != null) {
                m.setAccessible(true);
                Type type = m.getGenericParameterTypes()[0];
                Class<?> propertyType = getRawType(type);
                Class<?> genericType = getGenericType(type);
                map.put(propertyName, new PropertySetter() {
                    public Class<?> getPropertyType() {
                        return propertyType;
                    }
                    public Class<?> getGenericType() {
                        return genericType;
                    }
                    public void setProperty(Object obj, Object value) throws Exception {
                        m.invoke(obj, value);
                    }
                });
            }
        }
        Map<String, Field> fields = getAllFields(clazz);
        for (String propertyName : fields.keySet()) {
            Field f = fields.get(propertyName);
            if (! map.containsKey(propertyName)) {
                f.setAccessible(true);
                Type type = f.getGenericType();
                Class<?> propertyType = getRawType(type);
                Class<?> genericType = getGenericType(type);
                map.put(propertyName, new PropertySetter() {
                    public Class<?> getPropertyType() {
                        return propertyType;
                    }
                    public Class<?> getGenericType() {
                        return genericType;
                    }
                    public void setProperty(Object obj, Object value) throws Exception {
                        f.set(obj, value);
                    }
                });
            }
        }
        this.map = map;
    }

    Map<String, Method> getAllMethods(Class<?> clazz) {
        Map<String, Method> methods = new HashMap<String, Method>();
        while (clazz != null) {
            for (Method m : clazz.getDeclaredMethods()) {
                String propertyName = getPropertyName(m);
                if (! methods.containsKey(propertyName)) {
                    methods.put(propertyName, m);
                }
            }
            clazz = clazz.getSuperclass();
        }
        return methods;
    }

    Map<String, Field> getAllFields(Class<?> clazz) {
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

    /**
     * Get raw type of property, e.g. String, List, JavaBean, etc.
     */
    Class<?> getRawType(Type type) {
        if (type instanceof ParameterizedType) {
            ParameterizedType pt = (ParameterizedType) type;
            return (Class<?>) pt.getRawType();
        }
        return (Class<?>) type;
    }

    /**
     * Get generic type of property, e.g. generic type of List<String> is String. 
     * Return null if no generic type.
     */
    Class<?> getGenericType(Type type) {
        if (type instanceof ParameterizedType) {
            ParameterizedType pt = (ParameterizedType) type;
            Type[] ts = pt.getActualTypeArguments();
            if (ts.length == 1) {
                return (Class<?>) ts[0];
            }
        }
        Class<?> clazz = (Class<?>) type;
        if (clazz.isArray()) {
            return clazz.getComponentType();
        }
        return null;
    }

    PropertySetter getPropertySetter(String name) {
        return this.map.get(name);
    }

    String getPropertyName(Method m) {
        String name = m.getName();
        if (name.startsWith("set") && (name.length() >= 4)
                && m.getReturnType().equals(void.class)
                && (m.getParameterTypes().length == 1)
        ) {
            return Character.toLowerCase(name.charAt(3)) + name.substring(4);
        }
        return null;
    }

}

interface Converter {
    Object convert(Object value);
}

interface PropertySetter {

    Class<?> getGenericType();

    Class<?> getPropertyType();

    void setProperty(Object obj, Object value) throws Exception;

}
