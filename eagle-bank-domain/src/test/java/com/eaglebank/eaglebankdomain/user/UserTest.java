package com.eaglebank.eaglebankdomain.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    private FirstName fn;
    private LastName ln;
    private DateOfBirth dob;
    private EmailAddress email;
    private PasswordHash hash;
    private PhoneNumber phoneNumber;
    private Address address;

    @BeforeEach
    void setUp() {
        fn = new FirstName("Alice");
        ln = new LastName("Smith");
        dob = new DateOfBirth(LocalDate.now().minusYears(30));
        email = new EmailAddress("alice.smith@example.com");
        hash = new PasswordHash("dummyHash");
        phoneNumber = new PhoneNumber("+447911123456");
        address = new Address("123 High Street", "London", "Greater London", "SW1A 1AA");
    }

    @Test
    void shouldCreateUserWithValidData() {
        User user = User.create(fn, ln, dob, email, phoneNumber, address, hash);

        assertNotNull(user.getId(), "UserId should not be null");
        assertNotNull(user.getId().value(), "Underlying UUID should not be null");
        assertEquals(fn, user.getFirstName());
        assertEquals(ln, user.getLastName());
        assertEquals(dob, user.getDob());
        assertEquals(email, user.getEmail());
        assertEquals(phoneNumber, user.getPhoneNumber());
        assertEquals(address, user.getAddress());
        assertEquals(hash, user.getPasswordHash());
    }

    @Test
    void shouldThrowNullPointerWhenFirstNameIsNull() {
        assertThrows(NullPointerException.class, () ->
                User.create(null, ln, dob, email, phoneNumber, address, hash)
        );
    }

    @Test
    void shouldThrowNullPointerWhenLastNameIsNull() {
        assertThrows(NullPointerException.class, () ->
                User.create(fn, null, dob, email, phoneNumber, address, hash)
        );
    }

    @Test
    void shouldThrowNullPointerWhenDobIsNull() {
        assertThrows(NullPointerException.class, () ->
                User.create(fn, ln, null, email, phoneNumber, address, hash)
        );
    }

    @Test
    void shouldThrowNullPointerWhenEmailIsNull() {
        assertThrows(NullPointerException.class, () ->
                User.create(fn, ln, dob, null, phoneNumber, address, hash)
        );
    }

    @Test
    void shouldThrowNullPointerWhenHashIsNull() {
        assertThrows(NullPointerException.class, () ->
                User.create(fn, ln, dob, email, phoneNumber, address, null)
        );
    }

    @Test
    void withEmailShouldReturnNewUserWithUpdatedEmail() {
        User original = User.create(fn, ln, dob, email, phoneNumber, address, hash);
        EmailAddress newEmail = new EmailAddress("alice.new@example.com");
        FirstName newFirstName = new FirstName("Owen");
        LastName newLastName = new LastName("Cornock");
        DateOfBirth newDateOfBirth = new DateOfBirth(LocalDate.now().minusYears(20));

        User updated = original
                .withEmail(newEmail)
                .withDob(newDateOfBirth)
                .withFirstName(newFirstName)
                .withLastName(newLastName);

        assertEquals(email, original.getEmail());
        assertEquals(newEmail, updated.getEmail());
        assertEquals(fn, original.getFirstName());
        assertEquals(newFirstName, updated.getFirstName());
        assertEquals(ln, original.getLastName());
        assertEquals(newLastName, updated.getLastName());
        assertEquals(dob, original.getDob());
        assertEquals(newDateOfBirth, updated.getDob());
        assertEquals(hash, updated.getPasswordHash());
        assertEquals(phoneNumber, updated.getPhoneNumber());
        assertEquals(address, updated.getAddress());
        assertEquals(original.getId(), updated.getId());
    }

    @Test
    void shouldProduceCorrectToString() {
        User user = User.create(fn, ln, dob, email, phoneNumber, address, hash);
        String toString = user.toString();

        assertTrue(toString.startsWith("User("));
        assertTrue(toString.endsWith(")"));
        assertTrue(toString.contains("id=" + user.getId()));
        assertTrue(toString.contains("firstName=" + fn));
        assertTrue(toString.contains("lastName=" + ln));
        assertTrue(toString.contains("dob=" + dob));
        assertTrue(toString.contains("email=" + email));
        assertTrue(toString.contains("phoneNumber=" + phoneNumber));
        assertTrue(toString.contains("address=" + address));
        assertTrue(toString.contains("passwordHash=" + hash));
    }

    @Test
    void shouldRehydrateUserWithGivenValues() {
        UserId id = UserId.newId();
        User rehydrated = User.rehydrate(id, fn, ln, dob, email, phoneNumber, address, hash);

        assertSame(id, rehydrated.getId());
        assertSame(fn, rehydrated.getFirstName());
        assertSame(ln, rehydrated.getLastName());
        assertSame(dob, rehydrated.getDob());
        assertSame(email, rehydrated.getEmail());
        assertSame(phoneNumber, rehydrated.getPhoneNumber());
        assertSame(address, rehydrated.getAddress());
        assertSame(hash, rehydrated.getPasswordHash());
    }

    @Test
    void rehydrateShouldThrowNullPointerForNullId() {
        assertThrows(NullPointerException.class, () ->
                User.rehydrate(null, fn, ln, dob, email, phoneNumber, address, hash)
        );
    }

    @Test
    void rehydrateShouldThrowNullPointerForNullFirstName() {
        assertThrows(NullPointerException.class, () ->
                User.rehydrate(UserId.newId(), null, ln, dob, email, phoneNumber, address, hash)
        );
    }

    @Test
    void rehydrateShouldThrowNullPointerForNullLastName() {
        assertThrows(NullPointerException.class, () ->
                User.rehydrate(UserId.newId(), fn, null, dob, email, phoneNumber, address, hash)
        );
    }

    @Test
    void rehydrateShouldThrowNullPointerForNullDob() {
        assertThrows(NullPointerException.class, () ->
                User.rehydrate(UserId.newId(), fn, ln, null, email, phoneNumber, address, hash)
        );
    }

    @Test
    void rehydrateShouldThrowNullPointerForNullEmail() {
        assertThrows(NullPointerException.class, () ->
                User.rehydrate(UserId.newId(), fn, ln, dob, null, phoneNumber, address, hash)
        );
    }

    @Test
    void rehydrateShouldThrowNullPointerForNullHash() {
        assertThrows(NullPointerException.class, () ->
                User.rehydrate(UserId.newId(), fn, ln, dob, email, phoneNumber, address, null)
        );
    }

    @Test
    void shouldThrowNullPointerWhenPhoneNumberIsNull() {
        assertThrows(NullPointerException.class, () ->
            User.create(fn, ln, dob, email, null, address, hash)
        );
    }

    @Test
    void shouldThrowNullPointerWhenAddressIsNull() {
        assertThrows(NullPointerException.class, () ->
            User.create(fn, ln, dob, email, phoneNumber, null, hash)
        );
    }

    @Test
    void withPhoneNumberShouldReturnNewUserWithUpdatedPhoneNumber() {
        User original = User.create(fn, ln, dob, email, phoneNumber, address, hash);
        PhoneNumber newPhone = new PhoneNumber("+447911999999");
        
        User updated = original.withPhoneNumber(newPhone);
        
        assertEquals(phoneNumber, original.getPhoneNumber());
        assertEquals(newPhone, updated.getPhoneNumber());
        assertEquals(original.getId(), updated.getId());
    }

    @Test
    void withAddressShouldReturnNewUserWithUpdatedAddress() {
        User original = User.create(fn, ln, dob, email, phoneNumber, address, hash);
        Address newAddress = new Address(
            "456 New Street", "Manchester", "Greater Manchester", "M1 1AA"
        );
        
        User updated = original.withAddress(newAddress);
        
        assertEquals(address, original.getAddress());
        assertEquals(newAddress, updated.getAddress());
        assertEquals(original.getId(), updated.getId());
    }
}