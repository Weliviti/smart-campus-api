package com.smartcampus.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ApiErrorTest {

    @Test
    void ctor_setsFieldsAndTimestamp() {
        long before = System.currentTimeMillis();
        ApiError err = new ApiError(409, "ROOM_NOT_EMPTY",
                "Room still has sensors");
        long after = System.currentTimeMillis();

        assertEquals(409, err.getStatus());
        assertEquals("ROOM_NOT_EMPTY", err.getError());
        assertEquals("Room still has sensors", err.getMessage());
        assertTrue(err.getTimestamp() >= before && err.getTimestamp() <= after,
                "timestamp must be close to creation time");
    }

    @Test
    void defaultCtor_hasTimestampOnly() {
        ApiError err = new ApiError();
        assertEquals(0, err.getStatus());
        assertNull(err.getError());
        assertNull(err.getMessage());
        assertTrue(err.getTimestamp() > 0,
                "default ctor still stamps the creation time");
    }

    @Test
    void settersAllowMutation() {
        ApiError err = new ApiError();
        err.setStatus(500);
        err.setError("INTERNAL_ERROR");
        err.setMessage("oops");
        err.setTimestamp(1L);

        assertEquals(500, err.getStatus());
        assertEquals("INTERNAL_ERROR", err.getError());
        assertEquals("oops", err.getMessage());
        assertEquals(1L, err.getTimestamp());
    }
}
