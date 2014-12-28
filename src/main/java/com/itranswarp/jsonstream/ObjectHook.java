package com.itranswarp.jsonstream;

import java.util.Map;

public interface ObjectHook<T> {

    T toObject(Map<String, Object> map);

}
