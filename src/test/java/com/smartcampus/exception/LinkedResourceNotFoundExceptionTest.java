package com.smartcampus.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Basic tests for LinkedResourceNotFoundException. Used when a POST
 * references a resource that does not exist (e.g. a sensor pointing at
 * a non-existent roomId). Mapped to HTTP 422.
 */
class LinkedResourceNotFoundExceptionTest {

    @Test
    void extendsRuntimeException() {
        LinkedResourceNotFoundException ex =
                new LinkedResourceNotFoundException("Room", "NOPE");
        assertTrue(ex instanceof RuntimeException);
    }

    @Test
    void storesTypeAndId() {
        LinkedResourceNotFoundException ex =
                new LinkedResourceNotFoundException("Room", "LIB-999");
        assertEquals("Room", ex.getLinkedType());
        assertEquals("LIB-999", ex.getLinkedId());
    }

    @Test
    void messageMentionsLinkedType() {
        LinkedResourceNotFoundException ex =
                new LinkedResourceNotFoundException("Room", "LIB-999");
        assertTrue(ex.getMessage().contains("Room"));
    }

    @Test
    void messageMentionsLinkedId() {
        LinkedResourceNotFoundException ex =
                new LinkedResourceNotFoundException("Room", "LIB-999");
        assertTrue(ex.getMessage().contains("LIB-999"));
    }
}
