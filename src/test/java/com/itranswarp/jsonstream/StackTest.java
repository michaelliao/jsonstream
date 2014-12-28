package com.itranswarp.jsonstream;

import static org.junit.Assert.*;

import java.util.EmptyStackException;

import org.junit.Before;
import org.junit.Test;

public class StackTest {

    Stack stack = null;

    @Before
    public void setUp() throws Exception {
        stack = new Stack();
    }

    @Test
    public void testIsEmpty() {
        assertTrue(stack.isEmpty());
        stack.push(StackValue.newJsonSingle(1));
        assertFalse(stack.isEmpty());
        stack.pop(StackValue.TYPE_SINGLE);
        assertTrue(stack.isEmpty());
    }

    @Test
    public void testPushPeekAndPop() {
        assertTrue(stack.isEmpty());
        // push:
        stack.push(StackValue.newJsonSingle(1));
        stack.push(StackValue.newJsonSingle(2));
        // peek:
        assertEquals(2, stack.peek(StackValue.TYPE_SINGLE).value);
        // push:
        stack.push(StackValue.newJsonSingle(3));
        stack.push(StackValue.newJsonSingle(4));
        // pop:
        assertEquals(4, stack.pop(StackValue.TYPE_SINGLE).value);
        assertEquals(3, stack.pop(StackValue.TYPE_SINGLE).value);
        // push:
        stack.push(StackValue.newJsonSingle(5));
        stack.push(StackValue.newJsonSingle(6));
        // peek:
        assertEquals(6, stack.peek(StackValue.TYPE_SINGLE).value);
        // pop:
        assertEquals(6, stack.pop(StackValue.TYPE_SINGLE).value);
        assertEquals(5, stack.pop(StackValue.TYPE_SINGLE).value);
        assertEquals(2, stack.pop(StackValue.TYPE_SINGLE).value);
        assertEquals(1, stack.pop(StackValue.TYPE_SINGLE).value);
        assertTrue(stack.isEmpty());
    }

    @Test(expected = EmptyStackException.class)
    public void testPopFailed() {
        stack.pop(StackValue.TYPE_SINGLE);
    }

    @Test(expected = EmptyStackException.class)
    public void testPeekFailed() {
        stack.peek(StackValue.TYPE_SINGLE);
    }

    @Test(expected = StackOverflowError.class)
    public void testStackOverflow() {
        try {
            for (int i = 0; i < 100; i++) {
                stack.push(StackValue.newJsonSingle(i));
            }
        }
        catch (Throwable t) {
            fail("should not throw anything!");
        }
        stack.push(StackValue.newJsonSingle(0));
    }
}
