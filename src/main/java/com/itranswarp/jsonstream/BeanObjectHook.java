package com.itranswarp.jsonstream;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Convert JSON object (Map<String, Object>) to JavaBean object, and recursively if necessary.
 * 
 * @author Michael Liao
 */
public class BeanObjectHook implements ObjectHook {

    // cache for PropertySetters:
    Map<String, PropertySetters> setters = new HashMap<String, PropertySetters>();

    /**
     * Default constructor.
     */
    public BeanObjectHook() {
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object toObject(Map<String, Object> map, Class<?> clazz) {
        PropertySetters pss = setters.get(clazz.getName());
        if (pss == null) {
            pss = new PropertySetters(clazz);
            setters.put(clazz.getName(), pss);
        }
        try {
            Object target = newInstance(clazz, map);
            for (String propertyName : map.keySet()) {
                PropertySetter ps = pss.getPropertySetter(propertyName);
                if (ps != null) {
                    Object value = map.get(propertyName);
                    if (value instanceof Map) {
                        // nested JSON object:
                        value = toObject((Map<String, Object>) value, ps.getPropertyType());
                    }
                    else if (value instanceof List) {
                        // nested JSON array:
                        Class<?> propertyType = ps.getPropertyType();
                        if (propertyType.isAssignableFrom(List.class) || propertyType.isArray()) {
                            // set to List<T> or T[]:
                            Class<?> genericType = ps.getGenericType();
                            List<Object> valueList = (List<Object>) value;
                            // convert to List<T>:
                            List<Object> resultList = new ArrayList<Object>(valueList.size());
                            for (Object element : valueList) {
                                Object ele = isSimpleValue(element) ? element : toObject((Map<String, Object>) element, genericType);
                                resultList.add(ele);
                            }
                            if (propertyType.isArray()) {
                                // convert List<T> to T[]:
                                Object array = Array.newInstance(genericType, resultList.size());
                                int index=  0;
                                for (Object element : resultList) {
                                    Array.set(array, index, isSimpleValue(element)
                                            ? toSimpleValue(genericType, element)
                                                    : toObject((Map<String, Object>) element, genericType));
                                    index ++;
                                }
                                value = array;
                            }
                            else {
                                value = resultList;
                            }
                        }
                        else {
                            throw new JsonBindException("Cannot set Json array to property: " + propertyName + "(type: " + propertyType.getName() + ")");
                        }
                    }
                    ps.setProperty(target, value);
                }
            }
            return target;
        }
        catch (RuntimeException e) {
            throw e;
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Convert a simple value object to specific type.
     * 
     * @param genericType Object type: int.class, String.class, Float.class, etc.
     * @param element Value object.
     * @return Converted object.
     */
    Object toSimpleValue(Class<?> genericType, Object element) {
        if (element == null) {
            return null;
        }
        Converter converter = NUMBER_CONVERTERS.get(genericType.getName());
        if (converter != null) {
            return converter.convert(element);
        }
        return element;
    }

    static final Map<String, Converter> NUMBER_CONVERTERS;

    static {
        NUMBER_CONVERTERS = new HashMap<String, Converter>();
        Converter intConveter = (value) -> {
            if (value instanceof Long) {
                return ((Long) value).intValue();
            }
            throw new NumberFormatException("Cannot convert double to integer.");

        };
        Converter shortConveter = (value) -> {
            if (value instanceof Long) {
                return ((Long) value).shortValue();
            }
            throw new NumberFormatException("Cannot convert double to short.");

        };
        Converter byteConveter = (value) -> {
            if (value instanceof Long) {
                return ((Long) value).byteValue();
            }
            throw new NumberFormatException("Cannot convert double to byte.");

        };
        Converter floatConveter = (value) -> {
            if (value instanceof Float) {
                return ((Float) value).floatValue();
            }
            throw new NumberFormatException("Cannot convert long to float.");

        };
        Converter bigIntegerConveter = (value) -> {
            if (value instanceof Long) {
                return new BigInteger(value.toString());
            }
            throw new NumberFormatException("Cannot convert double to BigInteger.");
        };
        Converter bigDecimalConveter = (value) -> {
            if (value instanceof Double) {
                return new BigDecimal((Double) value);
            }
            throw new NumberFormatException("Cannot convert long to BigDecimal.");
        };
        NUMBER_CONVERTERS.put(int.class.getName(), intConveter);
        NUMBER_CONVERTERS.put(Integer.class.getName(), intConveter);
        NUMBER_CONVERTERS.put(short.class.getName(), shortConveter);
        NUMBER_CONVERTERS.put(Short.class.getName(), shortConveter);
        NUMBER_CONVERTERS.put(byte.class.getName(), byteConveter);
        NUMBER_CONVERTERS.put(Byte.class.getName(), byteConveter);
        NUMBER_CONVERTERS.put(float.class.getName(), floatConveter);
        NUMBER_CONVERTERS.put(Float.class.getName(), floatConveter);
        NUMBER_CONVERTERS.put(BigInteger.class.getName(), bigIntegerConveter);
        NUMBER_CONVERTERS.put(BigDecimal.class.getName(), bigDecimalConveter);
    }

    static final Set<String> SIMPLE_VALUE_NAMES = new HashSet<String>(
            Arrays.asList("java.lang.String",
                    "boolean", "java.lang.Boolean",
                    "long", "java.lang.Long",
                    "double", "java.lang.Double"));

    boolean isSimpleValue(Object obj) {
        if (obj == null) {
            return true;
        }
        String name = obj.getClass().getName();
        return SIMPLE_VALUE_NAMES.contains(name);
    }

    /**
     * Create a new JavaBean instance.
     * 
     * @param clazz JavaBean class.
     * @param jsonObject The Json object as Map.
     * @return JavaBean instance.
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    protected Object newInstance(Class<?> clazz, Map<String, Object> jsonObject) throws InstantiationException, IllegalAccessException {
        return clazz.newInstance();
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
