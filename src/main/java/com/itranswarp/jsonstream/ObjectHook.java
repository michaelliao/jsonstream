package com.itranswarp.jsonstream;

import java.util.Map;

public interface ObjectHook {

    Object toObject(Map<String, Object> map, Class<?> clazz);

}
