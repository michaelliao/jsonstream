package com.itranswarp.jsonstream;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

class PropertyGetters {

    final Class<?> clazz;
    final Map<String, PropertyGetter> map;

    PropertyGetters(Class<?> clazz) {
        this.clazz = clazz;
        Map<String, PropertyGetter> map = new HashMap<String, PropertyGetter>();
        Set<String> ignoredProperties = new HashSet<String>();
        Map<String, Method> getters = PropertyUtils.getAllGetters(clazz);
        for (String propertyName : getters.keySet()) {
            Method m = getters.get(propertyName);
            if (m==null) {
                ignoredProperties.add(propertyName);
            }
            else {
                m.setAccessible(true);
                map.put(propertyName, new PropertyGetter() {
                    public Object getProperty(Object obj) throws Exception {
                        return m.invoke(obj);
                    }
                });
            }
        }
        Map<String, Field> fields = PropertyUtils.getAllFields(clazz);
        for (String propertyName : fields.keySet()) {
            if (! map.containsKey(propertyName) && ! ignoredProperties.contains(propertyName)) {
                Field f = fields.get(propertyName);
                f.setAccessible(true);
                map.put(propertyName, new PropertyGetter() {
                    public Object getProperty(Object obj) throws Exception {
                        return f.get(obj);
                    }
                });
            }
        }
        this.map = map;
    }

    PropertyGetter getPropertyGetter(String name) {
        return this.map.get(name);
    }

    interface PropertyGetter {
    
        Object getProperty(Object obj) throws Exception;

    }
}

