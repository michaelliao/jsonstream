package com.itranswarp.jsonstream;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itranswarp.jsonstream.validator.Validator;

/**
 * Convert JSON object {@code Map<String, Object>} to JavaBean object, and
 * recursively if necessary.
 * 
 * @author Michael Liao
 */
public class BeanObjectMapper implements ObjectMapper {

    final Log log = LogFactory.getLog(getClass());
    final ObjectTypeFinder objectTypeFinder;

    // cache for PropertySetters:
    static Map<String, PropertySetters> cachedSetters = new ConcurrentHashMap<>();

    /**
     * Default constructor.
     */
    public BeanObjectMapper() {
        this.objectTypeFinder = (clazz, jsonObject) -> {
            return clazz;
        };
    }

    /**
     * Constructor with ObjectTypeFinder.
     * 
     * @param objectTypeFinder ObjectTypeFinder.
     */
    public BeanObjectMapper(ObjectTypeFinder objectTypeFinder) {
        this.objectTypeFinder = objectTypeFinder;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object toObject(String path, Map<String, Object> jsonObjectMap, Class<?> clazz, TypeAdapters typeAdapters) {
        clazz = this.objectTypeFinder.find(clazz, jsonObjectMap);
        String beanClassName = clazz.getName();
        log.info("Convert JSON object to bean: " + beanClassName);
        PropertySetters pss = cachedSetters.get(beanClassName);
        if (pss == null) {
            log.info("Load PropertySetters for class: " + beanClassName);
            pss = new PropertySetters(clazz);
            cachedSetters.put(beanClassName, pss);
        }
        try {
            // create new instance:
            Object target = newInstance(clazz, jsonObjectMap);
            for (String propertyName : pss.map.keySet()) {
                log.info("Try to set property: " + propertyName);
                PropertySetter ps = pss.getPropertySetter(propertyName);
                if (!jsonObjectMap.containsKey(propertyName)) {
                    // there is no property in the JSON object:
                    log.info("There is no corresponding value in the JSON object.");
                    if (ps.isRequired()) {
                        throw new JsonValidateException("Required", path + "." + propertyName);
                    }
                } else {
                    // json value is Map, List, String, Long, Double, Boolean and null:
                    Object jsonValue = jsonObjectMap.get(propertyName);
                    Class<?> propertyType = ps.getPropertyType();
                    if (jsonValue instanceof Map) {
                        log.info("Set nested JSON object to property: " + propertyName);
                        // nested JSON object:
                        jsonValue = toObject(path + "." + propertyName, (Map<String, Object>) jsonValue, propertyType, typeAdapters);
                    } else if (jsonValue instanceof List) {
                        log.info("Set nested JSON array to property: " + propertyName);
                        // nested JSON array:
                        if (propertyType.isAssignableFrom(List.class) || propertyType.isArray()) {
                            // set to List<T> or T[]:
                            Class<?> genericType = ps.getGenericType();
                            log.info("Nested array element type: " + genericType.getName());
                            List<Object> jsonValueList = (List<Object>) jsonValue;
                            // convert to List<T>:
                            List<Object> resultList = new ArrayList<>(jsonValueList.size());
                            int index = 0;
                            for (Object element : jsonValueList) {
                                log.info("Convert each element from JSON value to Java object...");
                                Object ele = isSimpleValue(element) ? toSimpleValue(genericType, element, typeAdapters)
                                        : ((element instanceof List) && Object.class.equals(genericType) // is List<Object>?
                                                ? element
                                                : toObject(path + "." + propertyName + "[" + index + "]", (Map<String, Object>) element, genericType,
                                                        typeAdapters)); // convert to List<T>
                                resultList.add(ele);
                                index++;
                            }
                            if (propertyType.isArray()) {
                                log.info("Convert to Java array: " + genericType.getName() + "[]...");
                                // convert List<T> to T[]:
                                Object array = Array.newInstance(genericType, resultList.size());
                                index = 0;
                                for (Object element : resultList) {
                                    Array.set(array, index, element);
                                    index++;
                                }
                                jsonValue = array;
                            } else {
                                jsonValue = resultList;
                            }
                        } else {
                            throw new JsonBindException("Cannot set Json array to property: " + propertyName + "(type: " + propertyType.getName() + ")");
                        }
                    } else {
                        Validator<?>[] validators = ps.getValidators();
                        if (validators != null && validators.length > 0) {
                            log.info("Validator simple value: " + jsonValue);
                            for (@SuppressWarnings("rawtypes")
                            Validator validator : validators) {
                                validator.validate(jsonValue, path, propertyName);
                            }
                        }
                        log.info("Set simple JSON value " + jsonValue + " to property: " + propertyName);
                        jsonValue = toSimpleValue(propertyType, jsonValue, typeAdapters);
                    }
                    ps.setProperty(target, jsonValue);
                }
            }
            return target;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Convert a simple value object to specific type. e.g. Long to int, String to
     * LocalDate.
     * 
     * @param genericType Object type: int.class, String.class, Float.class, etc.
     * @param element     Value object.
     * @return Converted object.
     */
    Object toSimpleValue(Class<?> genericType, Object element, TypeAdapters typeAdapters) {
        if (element == null) {
            return null;
        }
        log.info("Convert from " + element.getClass().getName() + " to " + genericType.getName());
        if (genericType.isEnum() && (element instanceof String)) {
            @SuppressWarnings({ "unchecked", "rawtypes" })
            Enum<?> enumValue = Enum.valueOf((Class<? extends Enum>) genericType, (String) element);
            return enumValue;
        }
        Converter converter = SIMPLE_VALUE_CONVERTERS.get(genericType.getName());
        if (converter != null) {
            return converter.convert(element);
        }
        if ((element instanceof String) && (typeAdapters != null)) {
            TypeAdapter<?> adapter = typeAdapters.getTypeAdapter(genericType);
            if (adapter != null) {
                return adapter.deserialize((String) element);
            }
        }
        return element;
    }

    static final Map<String, Converter> SIMPLE_VALUE_CONVERTERS;

    static {
        SIMPLE_VALUE_CONVERTERS = new HashMap<>();
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
            if (value instanceof Double) {
                return ((Double) value).floatValue();
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

    /**
     * Is the JSON value a simple value? Return true if the JSON value is String,
     * Long, Double, Boolean or null.
     * 
     * @param jsonObj JSON object.
     * @return True if this JSON object is a simple value.
     */
    boolean isSimpleValue(Object jsonObj) {
        if (jsonObj == null) {
            return true;
        }
        return (jsonObj instanceof String) || (jsonObj instanceof Boolean) || (jsonObj instanceof Long) || (jsonObj instanceof Double);
    }

    /**
     * Create a new JavaBean instance by invoke the default constructor.
     * 
     * @param clazz      JavaBean class.
     * @param jsonObject The Json object as Map.
     * @return JavaBean instance.
     * @throws Exception If any exception occur.
     */
    protected Object newInstance(Class<?> clazz, Map<String, Object> jsonObject) throws Exception {
        String key = clazz.getName();
        Constructor<?> cons = constructors.get(key);
        if (cons == null) {
            cons = clazz.getDeclaredConstructor();
            cons.setAccessible(true);
            constructors.put(key, cons);
        }
        return cons.newInstance();
    }

    Map<String, Constructor<?>> constructors = new ConcurrentHashMap<>();
}

/**
 * Internal converter used for BeanObjectHook.
 * 
 * @author Michael Liao
 */
interface Converter {
    Object convert(Object value);
}
