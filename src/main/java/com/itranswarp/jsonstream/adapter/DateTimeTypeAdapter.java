package com.itranswarp.jsonstream.adapter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.itranswarp.jsonstream.TypeAdapter;

/**
 * Standard DateTime type adapter for conversion between String and LocalDateTime.
 * 
 * @author Michael Liao
 */
public class DateTimeTypeAdapter implements TypeAdapter<LocalDateTime> {

    DateTimeFormatter formatter;

    public DateTimeTypeAdapter() {
        this.formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    }

    public DateTimeTypeAdapter(String pattern) {
        this.formatter = DateTimeFormatter.ofPattern(pattern);
    }

    public DateTimeTypeAdapter(DateTimeFormatter formatter) {
        this.formatter = formatter;
    }

    @Override
    public LocalDateTime deserialize(String s) {
        return LocalDateTime.parse(s, this.formatter);
    }

    @Override
    public String serialize(LocalDateTime t) {
        return t.format(this.formatter);
    }

}
