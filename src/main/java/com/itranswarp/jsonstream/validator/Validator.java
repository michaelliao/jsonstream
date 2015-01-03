package com.itranswarp.jsonstream.validator;

public interface Validator<T> {

	void validate(T obj, String path, String name);
}
