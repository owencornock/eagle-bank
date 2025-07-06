package com.eaglebank.eaglebankdomain.account;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SortCodeTest {

    @Test
    void shouldCreateValidSortCode() {
        SortCode code = new SortCode("123456");
        assertEquals("123456", code.value());
    }

    @Test
    void shouldThrowNullPointerExceptionWhenValueIsNull() {
        assertThrows(NullPointerException.class, () -> {
            new SortCode(null);
        }, "Sort code cannot be null");
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenValueIsEmpty() {
        assertThrows(IllegalArgumentException.class, () -> {
            new SortCode("");
        }, "Sort code cannot be empty");

        assertThrows(IllegalArgumentException.class, () -> {
            new SortCode("   ");
        }, "Sort code cannot be empty");
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenValueIsNotSixDigits() {
        assertThrows(IllegalArgumentException.class, () -> {
            new SortCode("12345");
        }, "Sort code must be exactly 6 digits");

        assertThrows(IllegalArgumentException.class, () -> {
            new SortCode("1234567");
        }, "Sort code must be exactly 6 digits");

        assertThrows(IllegalArgumentException.class, () -> {
            new SortCode("12345a");
        }, "Sort code must contain only digits");
    }

    @Test
    void shouldImplementEqualsAndHashCodeCorrectly() {
        SortCode code1 = new SortCode("123456");
        SortCode code2 = new SortCode("123456");
        SortCode code3 = new SortCode("654321");

        assertEquals(code1, code2, "Same sort codes should be equal");
        assertNotEquals(code1, code3, "Different sort codes should not be equal");
        assertNotEquals(null, code1, "Sort code should not be equal to null");
        assertNotEquals("123456", code1, "Sort code should not be equal to String");

        assertEquals(code1.hashCode(), code2.hashCode(),
                "Equal sort codes should have same hash code");
    }
}