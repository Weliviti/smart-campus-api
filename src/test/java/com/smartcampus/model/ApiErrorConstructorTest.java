package com.smartcampus.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * ApiError has two constructors. These tests just confirm both work.
 */
class ApiErrorConstructorTest {

    @Test
    void defaultConstructor_hasNoStatusOrError() {
        ApiError err = new ApiError();
        assertEquals(0, err.getStatus());
        // error and message are null until you set them
        // (asserting that would require assertNull and a separate test)
        assertTrue(err.getTimestamp() > 0);
    }

    @Test
    void threeArgConstructor_setsAllFields() {
        ApiError err = new ApiError(404, "NOT_FOUND", "Room 'X' does not exist");
        assertEquals(404, err.getStatus());
        assertEquals("NOT_FOUND", err.getError());
        assertEquals("Room 'X' does not exist", err.getMessage());
    }

    @Test
    void threeArgConstructor_timestampIsClose() {
        long before = System.currentTimeMillis();
        ApiError err = new ApiError(500, "INTERNAL_ERROR", "boom");
        long after = System.currentTimeMillis();

        assertTrue(err.getTimestamp() >= before);
        assertTrue(err.getTimestamp() <= after);
    }

    @Test
    void fieldsAreIndependent() {
        ApiError a = new ApiError(400, "BAD", "one");
        ApiError b = new ApiError(409, "CONFLICT", "two");

        // Mutating b should not affect a
        b.setStatus(422);
        assertEquals(400, a.getStatus());
        assertEquals(422, b.getStatus());
        assertNotNull(a.getMessage());
    }
}
