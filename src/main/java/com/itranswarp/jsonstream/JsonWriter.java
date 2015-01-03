package com.itranswarp.jsonstream;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.itranswarp.jsonstream.PropertyGetters.PropertyGetter;

public class JsonWriter {

    static final int MAX_DEPTH = 100;

    final boolean canToString;
    final Writer writer;
    final Map<String, PropertyGetters> cachedGetters = new ConcurrentHashMap<String, PropertyGetters>();
    final TypeAdapters typeAdapters;

    public JsonWriter(TypeAdapters typeAdapters) {
        this.canToString = true;
        this.writer = new StringWriter(1024);
        this.typeAdapters = typeAdapters;
    }

    public JsonWriter(Writer writer, TypeAdapters typeAdapters) {
        this.canToString = false;
        this.writer = writer;
        this.typeAdapters = typeAdapters;
    }

    @Override
    public String toString() {
        if (this.canToString) {
            return writer.toString();
        }
        else {
            throw new RuntimeException("Cannot get String since this JsonWriter write to an external stream.");
        }
    }

    void writeNumber(Number n) throws IOException {
        writer.write(n.toString());
    }

    void writeBoolean(Boolean b) throws IOException {
        writer.write(b.toString());
    }

    void writeNull() throws IOException {
        writer.write("null");
    }

    void writeEnum(Enum<?> e) throws IOException {
        writeString(e.name());
    }

    void writeString(String s) throws IOException {
        if (s == null) {
            writeNull();
            return;
        }
        writer.write('\"');
        for (int i = 0; i < s.length(); i ++) {
            char ch = s.charAt(i);
            switch (ch) {
            case '\"':
                writer.write("\\\"");
                break;
            case '\\':
                writer.write("\\\\");
                break;
            case '/':
                writer.write("\\/");
                break;
            case '\b':
                writer.write("\\b");
                break;
            case '\f':
                writer.write("\\f");
                break;
            case '\n':
                writer.write("\\n");
                break;
            case '\r':
                writer.write("\\r");
                break;
            case '\t':
                writer.write("\\t");
                break;
            default:
                writer.write(ch);
            }
        }
        writer.write('\"');
    }

    void writeList(List<Object> list, TypeAdapters typeAdapters, int depth) throws IOException {
        if (depth > MAX_DEPTH) {
            throw new JsonSerializeException("Maximum depth of nested object.");
        }
        if (list == null) {
            writeNull();
            return;
        }
        if (list.isEmpty()) {
            writer.write("[]");
            return;
        }
        writer.write('[');
        boolean isFirst = true;
        for (Object t : list) {
            if (isFirst) {
                isFirst = false;
            }
            else {
                writer.write(',');
            }
            if (t == null) {
                writeNull();
            }
            else if (t instanceof Number) {
                writeNumber((Number) t);
            }
            else if (t instanceof Boolean) {
                writeBoolean((Boolean) t);
            }
            else if (t instanceof String) {
                writeString((String) t);
            }
            else if (t instanceof List) {
                @SuppressWarnings("unchecked")
                List<Object> l = (List<Object>) t;
                writeList(l, typeAdapters, depth + 1);
            }
            else if (t instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> m = (Map<String, Object>) t;
                writeMap(m, typeAdapters, depth + 1);
            }
            else if (t.getClass().isArray()) {
                writeArray(t, typeAdapters, depth + 1);
            }
            else {
                writeObject(t, typeAdapters, depth + 1);
            }
        }
        writer.write(']');
    }

    void writeArray(Object array, TypeAdapters typeAdapters, int depth) throws IOException {
        if (depth > MAX_DEPTH) {
            throw new JsonSerializeException("Maximum depth of nested object.");
        }
        if (array instanceof boolean[]) {
            writer.write(Arrays.toString((boolean[]) array));
        }
        else if (array instanceof int[]) {
            writer.write(Arrays.toString((int[]) array));
        }
        else if (array instanceof long[]) {
            writer.write(Arrays.toString((long[]) array));
        }
        else if (array instanceof float[]) {
            writer.write(Arrays.toString((float[]) array));
        }
        else if (array instanceof double[]) {
            writer.write(Arrays.toString((double[]) array));
        }
        else if (array instanceof short[]) {
            writer.write(Arrays.toString((short[]) array));
        }
        else if (array instanceof byte[]) {
            writer.write(Arrays.toString((byte[]) array));
        }
        else {
            writeList(Arrays.asList((Object[]) array), typeAdapters, depth);
        }
    }

    void writeMap(Map<String, Object> map, TypeAdapters typeAdapters, int depth) throws IOException {
        if (depth > MAX_DEPTH) {
            throw new JsonSerializeException("Maximum depth of nested object.");
        }
        if (map == null) {
            writeNull();
            return;
        }
        if (map.isEmpty()) {
            writer.write("{}");
            return;
        }
        writer.write('{');
        boolean isFirst = true;
        for (String propertyName : map.keySet()) {
            Object propertyValue = map.get(propertyName);
            if (isFirst) {
                isFirst = false;
            }
            else {
                writer.write(',');
            }
            writer.write('\"');
            writer.write(propertyName);
            writer.write("\":");
            write(propertyValue, depth + 1);
        }
        writer.write('}');
    }

    void writeObject(Object bean, TypeAdapters typeAdapters, int depth) throws IOException {
        if (depth > MAX_DEPTH) {
            throw new JsonSerializeException("Maximum depth of nested object.");
        }
        if (bean == null) {
            writeNull();
            return;
        }
        Class<? extends Object> beanClass = bean.getClass();
        if (typeAdapters != null) {
            @SuppressWarnings("rawtypes")
            TypeAdapter adapter = typeAdapters.getTypeAdapter(beanClass);
            if (adapter != null) {
                @SuppressWarnings("unchecked")
                String result = adapter.serialize(bean);
                writeString(result);
                return;
            }
        }
        String className = beanClass.getName();
        PropertyGetters pgs = cachedGetters.get(className);
        if (pgs == null) {
            pgs = new PropertyGetters(bean.getClass());
            cachedGetters.put(className, pgs);
        }
        Map<String, PropertyGetter> getters = pgs.map;
        if (getters.isEmpty()) {
            writer.write("{}");
            return;
        }
        try {
            writer.write('{');
            boolean isFirst = true;
            for (String propertyName : getters.keySet()) {
                if (isFirst) {
                    isFirst = false;
                }
                else {
                    writer.write(',');
                }
                PropertyGetter pg = getters.get(propertyName);
                Object obj = pg.getProperty(bean);
                writer.write('\"');
                writer.write(propertyName);
                writer.write("\":");
                write(obj, depth);
            }
            writer.write('}');
        }
        catch (RuntimeException | IOException e) {
            throw e;
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public JsonWriter write(Object obj) throws IOException {
        write(obj, 0);
        return this;
    }

    /**
     * Write any type of data with depth.
     * 
     * @param obj
     * @param depth
     * @throws IOException
     */
    @SuppressWarnings("unchecked")
    void write(Object obj, int depth) throws IOException {
        if (obj == null) {
            writeNull();
            return;
        }
        if (obj instanceof String) {
            writeString((String) obj);
            return;
        }
        if (obj instanceof List) {
            writeList((List<Object>) obj, typeAdapters, depth);
            return;
        }
        if (obj instanceof Boolean) {
            writeBoolean((Boolean) obj);
            return;
        }
        if (obj instanceof Number) {
            writeNumber((Number) obj);
            return;
        }
        Class<?> clazz = obj.getClass();
        if (clazz.isEnum()) {
            writeEnum((Enum<?>) obj);
            return;
        }
        if (clazz.isArray()) {
            writeArray(obj, typeAdapters, depth);
            return;
        }
        writeObject(obj, typeAdapters, depth);
    }
}
