package com.eaglebank.eaglebankdomain.user;

import com.eaglebank.eaglebankdomain.exception.InvalidUserDataException;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AddressTest {

    @Test
    void shouldCreateValidAddress() {
        Address address = new Address(
            "123 High Street",
            "London",
            "Greater London",
            "SW1A 1AA"
        );
        
        assertEquals("123 High Street", address.line1());
        assertEquals("London", address.town());
        assertEquals("Greater London", address.county());
        assertEquals("SW1A 1AA", address.postcode());
    }

    @Test
    void shouldThrowNullPointerExceptionForNullFields() {
        assertThrows(NullPointerException.class, () ->
            new Address(null, "London", "Greater London", "SW1A 1AA")
        );
        
        assertThrows(NullPointerException.class, () ->
            new Address("123 High Street", null, "Greater London", "SW1A 1AA")
        );
        
        assertThrows(NullPointerException.class, () ->
            new Address("123 High Street", "London", null, "SW1A 1AA")
        );
        
        assertThrows(NullPointerException.class, () ->
            new Address("123 High Street", "London", "Greater London", null)
        );
    }

    @Test
    void shouldThrowInvalidUserDataExceptionForEmptyFields() {
        assertThrows(InvalidUserDataException.class, () ->
            new Address("", "London", "Greater London", "SW1A 1AA")
        );
        
        assertThrows(InvalidUserDataException.class, () ->
            new Address("123 High Street", "", "Greater London", "SW1A 1AA")
        );
        
        assertThrows(InvalidUserDataException.class, () ->
            new Address("123 High Street", "London", "", "SW1A 1AA")
        );
        
        assertThrows(InvalidUserDataException.class, () ->
            new Address("123 High Street", "London", "Greater London", "")
        );
    }

    @Test
    void shouldValidatePostcodeFormat() {
        // Valid postcodes
        assertDoesNotThrow(() -> new Address(
            "123 High Street", "London", "Greater London", "SW1A 1AA"
        ));
        
        // Invalid postcodes
        assertThrows(InvalidUserDataException.class, () ->
            new Address("123 High Street", "London", "Greater London", "ABC 123")
        );
        
        assertThrows(InvalidUserDataException.class, () ->
            new Address("123 High Street", "London", "Greater London", "SW1A1AA")
        );
    }

    @Test
    void shouldTrimWhitespace() {
        Address address = new Address(
            "  123 High Street  ",
            "  London  ",
            "  Greater London  ",
            "  SW1A 1AA  "
        );
        
        assertEquals("123 High Street", address.line1());
        assertEquals("London", address.town());
        assertEquals("Greater London", address.county());
        assertEquals("SW1A 1AA", address.postcode());
    }

    @Test
    void shouldImplementEqualsAndHashCodeCorrectly() {
        Address addr1 = new Address("123 High St", "London", "Greater London", "SW1A 1AA");
        Address addr2 = new Address("123 High St", "London", "Greater London", "SW1A 1AA");
        Address addr3 = new Address("124 High St", "London", "Greater London", "SW1A 1AA");

        assertEquals(addr1, addr2, "Same addresses should be equal");
        assertNotEquals(addr1, addr3, "Different addresses should not be equal");
        assertNotEquals(addr1, null, "Address should not be equal to null");

        assertEquals(addr1.hashCode(), addr2.hashCode(),
                "Equal addresses should have same hash code");
    }

    @Test
    void shouldEnforceFieldLengthLimits() {
        String tooLongLine1 = "a".repeat(101);
        String tooLongTown = "a".repeat(51);
        String tooLongCounty = "a".repeat(51);

        assertThrows(InvalidUserDataException.class, () ->
            new Address(tooLongLine1, "London", "Greater London", "SW1A 1AA")
        );
        
        assertThrows(InvalidUserDataException.class, () ->
            new Address("123 High Street", tooLongTown, "Greater London", "SW1A 1AA")
        );
        
        assertThrows(InvalidUserDataException.class, () ->
            new Address("123 High Street", "London", tooLongCounty, "SW1A 1AA")
        );
    }
}