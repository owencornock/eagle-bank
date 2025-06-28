package com.eaglebank.eaglebankdomain.user;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;

class UserIdTest {

    @Test
    void shouldGiveNewUserId() {
        UserId id1 = UserId.newId();
        UserId id2 = UserId.newId();

        assertNotNull(id1, "newId() must not return null");
        assertNotNull(id1.value(), "underlying UUID must not be null");
        assertNotNull(id2, "newId() must not return null on subsequent call");
        assertNotNull(id2.value(), "underlying UUID must not be null on subsequent call");
        assertNotEquals(id1.value(), id2.value(),
                "two calls to newId() should produce different UUIDs");
    }

    @Test
    void shouldProduceUserIdFromUuid() {
        UUID uuid = UUID.randomUUID();
        UserId id = UserId.of(uuid);

        assertEquals(uuid, id.value(),
                "UserId.of(uuid) must store exactly the given UUID");
    }

    @Test
    void shouldThrowNullPointerExceptionWithNullValue() {
        assertThrows(NullPointerException.class, () -> {
            UserId.of(null);
        }, "Passing null to UserId.of(...) should throw NPE due to @NonNull");
    }
}
