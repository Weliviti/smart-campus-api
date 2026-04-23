package com.smartcampus.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Basic tests for RoomNotEmptyException. It should be a RuntimeException
 * (so resource methods do not have to declare it) and must carry the
 * roomId and sensor count for the mapper to include in the response.
 */
class RoomNotEmptyExceptionTest {

    @Test
    void extendsRuntimeException() {
        RoomNotEmptyException ex = new RoomNotEmptyException("LIB-301", 2);
        assertTrue(ex instanceof RuntimeException);
    }

    @Test
    void storesRoomIdAndCount() {
        RoomNotEmptyException ex = new RoomNotEmptyException("LIB-301", 3);
        assertEquals("LIB-301", ex.getRoomId());
        assertEquals(3, ex.getSensorCount());
    }

    @Test
    void messageMentionsRoomId() {
        RoomNotEmptyException ex = new RoomNotEmptyException("LIB-301", 1);
        assertTrue(ex.getMessage().contains("LIB-301"));
    }

    @Test
    void messageMentionsSensorCount() {
        RoomNotEmptyException ex = new RoomNotEmptyException("LIB-301", 5);
        assertTrue(ex.getMessage().contains("5"));
    }
}
