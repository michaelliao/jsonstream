package com.itranswarp.jsonstream.adapter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import com.itranswarp.jsonstream.TypeAdapter;

/**
 * Standard Date type adapter for conversion between String and LocalDate.
 * 
 * @author Michael Liao
 */
public class DateTypeAdapter implements TypeAdapter<LocalDate> {

    DateTimeFormatter formatter;

    public DateTypeAdapter() {
        this.formatter = DateTimeFormatter.ISO_LOCAL_DATE;
    }

    public DateTypeAdapter(String pattern) {
        this.formatter = DateTimeFormatter.ofPattern(pattern);
    }

    public DateTypeAdapter(DateTimeFormatter formatter) {
        this.formatter = formatter;
    }

    @Override
    public LocalDate deserialize(String s) {
        return LocalDate.parse(s, this.formatter);
    }

    @Override
    public String serialize(LocalDate t) {
        return t.format(this.formatter);
    }

}
