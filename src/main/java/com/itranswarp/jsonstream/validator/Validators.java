package com.itranswarp.jsonstream.validator;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

class Validators {

    static Map<String, Validator<?>> cachedValidators = new ConcurrentHashMap<String, Validator<?>>();

    static Validator<?> getValidator(Class<?> clazz, Method method) {
        String key = clazz.getName() + "/" + method.getName();
        Validator<?> validator = cachedValidators.get(key);
        if (validator == null) {
            validator = getValidator(method);
            cachedValidators.put(key, validator);
        }
        return validator;
    }

    static Validator<?> getValidator(Method method) {
        return null;
    }
}
