package com.eaglebank.eaglebankdomain.account;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class AccountIdTest {

    @Test
    void shouldGiveNewAccountId() {
        AccountId id1 = AccountId.newId();
        AccountId id2 = AccountId.newId();

        assertNotNull(id1, "newId() must not return null");
        assertNotNull(id1.value(), "underlying UUID must not be null");
        assertNotNull(id2, "newId() must not return null on subsequent call");
        assertNotNull(id2.value(), "underlying UUID must not be null on subsequent call");
        assertNotEquals(id1.value(), id2.value(),
                "two calls to newId() should produce different UUIDs");
    }

    @Test
    void shouldProduceAccountIdFromUuid() {
        UUID uuid = UUID.randomUUID();
        AccountId id = AccountId.of(uuid);

        assertEquals(uuid, id.value(),
                "AccountId.of(uuid) must store exactly the given UUID");
    }

    @Test
    void shouldThrowNullPointerExceptionWithNullValue() {
        assertThrows(NullPointerException.class, () -> {
            AccountId.of(null);
        }, "Passing null to AccountId.of(...) should throw NPE due to @NonNull");
    }

    @Test
    void shouldImplementEqualsAndHashCodeCorrectly() {
        UUID uuid = UUID.randomUUID();
        AccountId id1 = AccountId.of(uuid);
        AccountId id2 = AccountId.of(uuid);
        AccountId id3 = AccountId.of(UUID.randomUUID());

        // Test equals
        assertEquals(id1, id2, "AccountIds with same UUID should be equal");
        assertNotEquals(id1, id3, "AccountIds with different UUIDs should not be equal");
        assertNotEquals(id1, null, "AccountId should not be equal to null");
        assertNotEquals(id1, "not an AccountId", "AccountId should not be equal to other types");

        // Test hashCode
        assertEquals(id1.hashCode(), id2.hashCode(),
                "Equal AccountIds should have same hash code");
    }
}