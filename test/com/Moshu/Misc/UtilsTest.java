package com.Moshu.Misc;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class UtilsTest {

    @Test
    public void testIsInt() {
        assertTrue(Utils.isInt("123"));
        assertFalse(Utils.isInt("abc"));
        assertFalse(Utils.isInt("12.3"));
    }

    @Test
    public void testExtractInt() {
        assertEquals(123, Utils.extractInt("abc123def"));
        assertEquals(0, Utils.extractInt("abc"));
        assertEquals(45, Utils.extractInt("  45  "));
    }

    @Test
    public void testSetCapitals() {
        assertEquals("Hello", Utils.setCapitals("hello"));
        assertEquals("World", Utils.setCapitals("World"));
        assertEquals("", Utils.setCapitals(""));
    }
}
