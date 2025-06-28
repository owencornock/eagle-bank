package com.eaglebank.eaglebankdomain.user;

import com.eaglebank.eaglebankdomain.exception.InvalidUserDataException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class PasswordHashTest {

    @Test
    void shouldCreatePasswordHashWithValidValue() {
        String validHash = "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy";
        PasswordHash hash = new PasswordHash(validHash);
        assertEquals(validHash, hash.value());
    }

    @Test
    void shouldThrowExceptionWhenValueIsNull() {
        InvalidUserDataException exception = assertThrows(
                InvalidUserDataException.class,
                () -> new PasswordHash(null)
        );
        assertEquals("Password hash cannot be blank", exception.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "\t", "\n", "   "})
    void shouldThrowExceptionWhenValueIsBlank(String blankValue) {
        InvalidUserDataException exception = assertThrows(
                InvalidUserDataException.class,
                () -> new PasswordHash(blankValue)
        );
        assertEquals("Password hash cannot be blank", exception.getMessage());
    }

    @Test
    void shouldImplementEqualsCorrectly() {
        String hashValue = "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy";
        PasswordHash hash1 = new PasswordHash(hashValue);
        PasswordHash hash2 = new PasswordHash(hashValue);
        PasswordHash differentHash = new PasswordHash("$2a$10$different");

        assertEquals(hash1, hash2);
        assertNotEquals(hash1, differentHash);
        assertNotEquals(null, hash1);
        assertNotEquals("not a password hash", hash1);
    }

    @Test
    void shouldImplementHashCodeCorrectly() {
        String hashValue = "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy";
        PasswordHash hash1 = new PasswordHash(hashValue);
        PasswordHash hash2 = new PasswordHash(hashValue);

        assertEquals(hash1.hashCode(), hash2.hashCode());
    }

    @Test
    void shouldImplementToStringCorrectly() {
        String hashValue = "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy";
        PasswordHash hash = new PasswordHash(hashValue);
        String toString = hash.toString();

        assertTrue(toString.contains(hashValue));
        assertTrue(toString.contains("PasswordHash"));
    }
}