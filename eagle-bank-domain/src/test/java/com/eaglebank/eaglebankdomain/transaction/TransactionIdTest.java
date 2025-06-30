package com.eaglebank.eaglebankdomain.transaction;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TransactionIdTest {

    @Test
    void shouldGiveNewTransactionId() {
        TransactionId id1 = TransactionId.newId();
        TransactionId id2 = TransactionId.newId();

        assertNotNull(id1, "newId() must not return null");
        assertNotNull(id1.value(), "underlying UUID must not be null");
        assertNotNull(id2, "newId() must not return null on subsequent call");
        assertNotNull(id2.value(), "underlying UUID must not be null on subsequent call");
        assertNotEquals(id1.value(), id2.value(),
                "two calls to newId() should produce different UUIDs");
    }

    @Test
    void shouldProduceTransactionIdFromUuid() {
        UUID uuid = UUID.randomUUID();
        TransactionId id = TransactionId.of(uuid);

        assertEquals(uuid, id.value(),
                "TransactionId.of(uuid) must store exactly the given UUID");
    }

    @Test
    void shouldThrowNullPointerExceptionWithNullValue() {
        assertThrows(NullPointerException.class, () -> {
            TransactionId.of(null);
        }, "Passing null to TransactionId.of(...) should throw NPE");
    }

    @Test
    void shouldImplementEqualsAndHashCodeCorrectly() {
        UUID uuid = UUID.randomUUID();
        TransactionId id1 = TransactionId.of(uuid);
        TransactionId id2 = TransactionId.of(uuid);
        TransactionId id3 = TransactionId.of(UUID.randomUUID());

        assertEquals(id1, id2, "TransactionIds with same UUID should be equal");
        assertNotEquals(id1, id3, "TransactionIds with different UUIDs should not be equal");
        assertNotEquals(null, id1, "TransactionId should not be equal to null");
        assertNotEquals("not a TransactionId", id1,
                "TransactionId should not be equal to other types");

        assertEquals(id1.hashCode(), id2.hashCode(),
                "Equal TransactionIds should have same hash code");
    }
}