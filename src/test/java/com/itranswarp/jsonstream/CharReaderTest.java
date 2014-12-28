package com.itranswarp.jsonstream;

import static org.junit.Assert.*;

import java.io.Reader;
import java.io.StringReader;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public class CharReaderTest {

    String createString(int length) {
        StringBuilder sb = new StringBuilder(length);
        String block = "abcdefghi.";
        while (sb.length() < length) {
            sb.append(block);
        }
        return sb.substring(0, length);
    }

    Reader createReader(int length) {
        return new StringReader(createString(length));
    }

    @Test
    public void testHasMore() throws Exception {
        // only 1 char:
        CharReader reader = new CharReader(createReader(1));
        assertTrue(reader.hasMore());
        // consume 1 char:
        assertEquals('a', reader.next());
        assertFalse(reader.hasMore());
    }

    @Test
    public void testNextAndPeek() throws Exception {
        // buffer_size * 2 + 1 chars:
        String input = createString(CharReader.BUFFER_SIZE * 2 + 1);
        CharReader reader = new CharReader(new StringReader(input));
        StringBuffer output = new StringBuffer();
        assertTrue(reader.hasMore());
        // consume buffer_size * 2 chars:
        for (int i = 0; i < CharReader.BUFFER_SIZE * 2; i++) {
            char ch = reader.peek();
            char nxt = reader.next();
            assertEquals(ch, nxt);
            output.append(ch);
        }
        assertTrue(reader.hasMore());
        // consume last char:
        char ch = reader.peek();
        char nxt = reader.next();
        assertEquals(ch, nxt);
        output.append(ch);
        assertFalse(reader.hasMore());
        // check:
        assertEquals(input, output.toString());
    }

    @Test
    public void testEOF() throws Exception {
        List<Integer> lengths = Arrays.asList(0, 1, CharReader.BUFFER_SIZE - 1,
                CharReader.BUFFER_SIZE, CharReader.BUFFER_SIZE + 1,
                100 * CharReader.BUFFER_SIZE, 100 * CharReader.BUFFER_SIZE + 1);
        for (int length : lengths) {
            String input = createString(length);
            CharReader reader = new CharReader(new StringReader(input));
            StringBuilder output = new StringBuilder();
            // should consume all:
            for (int i = 0; i < length; i++) {
                assertTrue(reader.hasMore());
                output.append(reader.next());
            }
            assertFalse(reader.hasMore());
            try {
                reader.next();
                fail("Not caught ParseException!");
            }
            catch (JsonParseException e) {
                assertEquals("EOF", e.getMessage());
            }
            assertEquals(input, output.toString());
        }
    }

}
