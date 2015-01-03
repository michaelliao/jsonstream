package com.itranswarp.jsonstream;

import java.util.Map;

public interface ObjectHook {

    Object toObject(String path, Map<String, Object> map, Class<?> clazz, TypeAdapters typeAdapters);

}
