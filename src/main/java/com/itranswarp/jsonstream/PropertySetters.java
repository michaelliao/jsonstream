package com.itranswarp.jsonstream;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.itranswarp.jsonstream.annotation.Required;
import com.itranswarp.jsonstream.validator.Validator;
import com.itranswarp.jsonstream.validator.impl.BooleanValidator;
import com.itranswarp.jsonstream.validator.impl.IntegerValidator;
import com.itranswarp.jsonstream.validator.impl.NumberValidator;
import com.itranswarp.jsonstream.validator.impl.StringValidator;

/**
 * To set value to bean property by conversion if necessary.
 * 
 * @author Michael Liao
 */
class PropertySetters {

    static final Field[] EMPTY_FIELDS = new Field[0];
    static final Method[] EMPTY_METHODS = new Method[0];

    final Class<?> clazz;
    final Map<String, PropertySetter> map;

    PropertySetters(Class<?> clazz) {
        this.clazz = clazz;
        Map<String, PropertySetter> map = new HashMap<String, PropertySetter>();
        Set<String> ignoredProperties = new HashSet<String>();
        Map<String, Method> setters = PropertyUtils.getAllSetters(clazz);
        for (String propertyName : setters.keySet()) {
            Method m = setters.get(propertyName);
            if (m==null) {
                ignoredProperties.add(propertyName);
            }
            else {
                m.setAccessible(true);
                Type type = m.getGenericParameterTypes()[0];
                Class<?> propertyType = getRawType(type);
                Class<?> genericType = getGenericType(type);
                boolean isRequired = isRequired(m);
                Validator<?>[] validators = getValidators(m, propertyType);
                map.put(propertyName, new PropertySetter() {
                    public boolean isRequired() {
                        return isRequired;
                    }
                    public Class<?> getPropertyType() {
                        return propertyType;
                    }
                    public Class<?> getGenericType() {
                        return genericType;
                    }
                    public void setProperty(Object bean, Object value) throws Exception {
                        m.invoke(bean, value);
                    }
                    public Validator<?>[] getValidators() {
                        return validators;
                    }
                });
            }
        }
        Map<String, Field> fields = PropertyUtils.getAllFields(clazz);
        for (String propertyName : fields.keySet()) {
            if (! map.containsKey(propertyName) && ! ignoredProperties.contains(propertyName)) {
                Field f = fields.get(propertyName);
                f.setAccessible(true);
                Type type = f.getGenericType();
                Class<?> propertyType = getRawType(type);
                Class<?> genericType = getGenericType(type);
                boolean isRequired = isRequired(f);
                Validator<?>[] validators = getValidators(f, propertyType);
                map.put(propertyName, new PropertySetter() {
                    public boolean isRequired() {
                        return isRequired;
                    }
                    public Class<?> getPropertyType() {
                        return propertyType;
                    }
                    public Class<?> getGenericType() {
                        return genericType;
                    }
                    public void setProperty(Object bean, Object value) throws Exception {
                        f.set(bean, value);
                    }
                    public Validator<?>[] getValidators() {
                        return validators;
                    }
                });
            }
        }
        this.map = map;
    }

    boolean isRequired(AnnotatedElement ae) {
        return ae.isAnnotationPresent(Required.class);
    }

    Validator<?>[] getValidators(AnnotatedElement ae, Class<?> propertyType) {
        String className = propertyType.getName();
        switch (className) {
        case "boolean":
        case "java.lang.Boolean":
            return new Validator<?>[] { new BooleanValidator(ae) };
        case "long":
        case "int":
        case "short":
        case "byte":
        case "java.lang.Long":
        case "java.lang.Integer":
        case "java.lang.Short":
        case "java.lang.Byte":
        case "java.math.BigInteger":
            return new Validator<?>[] { new IntegerValidator(ae) };
        case "float":
        case "double":
        case "java.lang.Float":
        case "java.lang.Double":
        case "java.math.BigDecimal":
            return new Validator<?>[] { new NumberValidator(ae) };
        default:
            // all other types are treat as String:
            return new Validator<?>[] { new StringValidator(ae) };
        }
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
     * Return Class<Object> if no generic type.
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
        return Object.class;
    }

    PropertySetter getPropertySetter(String name) {
        return this.map.get(name);
    }

}

interface PropertySetter {

    boolean isRequired();

    Validator<?>[] getValidators();

    Class<?> getGenericType();

    Class<?> getPropertyType();

    void setProperty(Object obj, Object value) throws Exception;

}
