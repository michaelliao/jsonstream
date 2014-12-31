package com.itranswarp.jsonstream;

import java.util.HashMap;
import java.util.Map;

/**
 * Store a class-TypeAdapter map.
 * 
 * @author Michael Liao
 */
class TypeAdapters {

    Map<String, TypeAdapterWrapper<?>> adapters = new HashMap<String, TypeAdapterWrapper<?>>();

    public <T> void registerTypeAdapter(Class<T> clazz, TypeAdapter<T> typeAdapter) {
        adapters.put(clazz.getName(), new TypeAdapterWrapper<T>(clazz, typeAdapter));
    }

    /**
     * Get TypeAdapter by class, or null if not found.
     * 
     * @param clazz Class object.
     * @return TypeAdapter or null if not found.
     */
    @SuppressWarnings("unchecked")
    <T> TypeAdapter<T> getTypeAdapter(Class<T> clazz) {
        return (TypeAdapter<T>) adapters.get(clazz.getName());
    }

    class TypeAdapterWrapper<T> {
    
        final Class<T> clazz;
        final TypeAdapter<T> typeAdapter;
    
        public TypeAdapterWrapper(Class<T> clazz, TypeAdapter<T> typeAdapter) {
            this.clazz = clazz;
            this.typeAdapter = typeAdapter;
        }
    
        boolean accept(Object obj) {
            return clazz.isInstance(obj);
        }
    }

}
