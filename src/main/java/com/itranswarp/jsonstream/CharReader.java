package com.itranswarp.jsonstream;

import java.io.IOException;
import java.io.Reader;

/**
 * Provide easy way to read or fetch next char, and auto-fill its buffer.
 * 
 * @author Michael Liao
 */
class CharReader {

	static final int BUFFER_SIZE = 1024;

	// total readed chars:
	int readed = 0;

	// buffer position:
	int pos = 0;

	// buffer ends:
	int size = 0;

	final char[] buffer;
	final Reader reader;

	public CharReader(Reader reader) {
		this.buffer = new char[BUFFER_SIZE];
		this.reader = reader;
	}

	public boolean hasMore() throws IOException {
		if (pos < size) {
			return true;
		}
		// try fill:
		fillBuffer(null);
		return pos < size;
	}

	public String next(int size) throws IOException {
		StringBuilder sb = new StringBuilder(size);
		for (int i=0; i<size; i++) {
			sb.append(next());
		}
		return sb.toString();
	}

	public char next() throws IOException {
		if (this.pos==this.size) {
			// fill buffer:
			fillBuffer("EOF");
		}
		char ch = this.buffer[this.pos];
		this.pos ++;
		return ch;
	}

	public char peek() throws IOException {
		if (this.pos==this.size) {
			// fill buffer:
			fillBuffer("EOF");
		}
		assert(this.pos < this.size);
		return this.buffer[this.pos];
	}

	void fillBuffer(String eofErrorMessage) throws IOException {
		int n = reader.read(buffer);
		if (n==(-1)) {
			if (eofErrorMessage!=null) {
				throw new JsonParseException(eofErrorMessage, this.readed);
			}
			return;
		}
		this.pos = 0;
		this.size = n;
		this.readed += n;
	}
}
