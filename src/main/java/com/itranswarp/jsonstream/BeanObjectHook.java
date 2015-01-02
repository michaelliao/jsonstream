package com.itranswarp.jsonstream;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Convert JSON object (Map<String, Object>) to JavaBean object, and recursively if necessary.
 * 
 * @author Michael Liao
 */
public class BeanObjectHook implements ObjectHook {

    // cache for PropertySetters:
    static Map<String, PropertySetters> cachedSetters = new ConcurrentHashMap<String, PropertySetters>();

    /**
     * Default constructor.
     */
    public BeanObjectHook() {
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object toObject(Map<String, Object> map, Class<?> clazz) {
        PropertySetters pss = cachedSetters.get(clazz.getName());
        if (pss == null) {
            pss = new PropertySetters(clazz);
            cachedSetters.put(clazz.getName(), pss);
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
                    else {
                        value = toSimpleValue(ps.getPropertyType(), value);
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
        if (genericType.isEnum() && (element instanceof String)) {
            @SuppressWarnings({ "unchecked", "rawtypes" })
            Enum<?> enumValue = Enum.valueOf((Class<? extends Enum>) genericType, (String) element);
            return enumValue;
        }
        Converter converter = SIMPLE_VALUE_CONVERTERS.get(genericType.getName());
        if (converter != null) {
            return converter.convert(element);
        }
        return element;
    }

    static final Map<String, Converter> SIMPLE_VALUE_CONVERTERS;

    static {
        SIMPLE_VALUE_CONVERTERS = new HashMap<String, Converter>();
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
        SIMPLE_VALUE_CONVERTERS.put(int.class.getName(), intConveter);
        SIMPLE_VALUE_CONVERTERS.put(Integer.class.getName(), intConveter);
        SIMPLE_VALUE_CONVERTERS.put(short.class.getName(), shortConveter);
        SIMPLE_VALUE_CONVERTERS.put(Short.class.getName(), shortConveter);
        SIMPLE_VALUE_CONVERTERS.put(byte.class.getName(), byteConveter);
        SIMPLE_VALUE_CONVERTERS.put(Byte.class.getName(), byteConveter);
        SIMPLE_VALUE_CONVERTERS.put(float.class.getName(), floatConveter);
        SIMPLE_VALUE_CONVERTERS.put(Float.class.getName(), floatConveter);
        SIMPLE_VALUE_CONVERTERS.put(BigInteger.class.getName(), bigIntegerConveter);
        SIMPLE_VALUE_CONVERTERS.put(BigDecimal.class.getName(), bigDecimalConveter);
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

