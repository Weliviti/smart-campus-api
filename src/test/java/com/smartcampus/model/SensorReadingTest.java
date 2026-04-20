package com.smartcampus.model;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class SensorReadingTest {

    @Test
    void valueConstructor_setsIdAndTimestamp() {
        long before = System.currentTimeMillis();
        SensorReading r = new SensorReading(23.7);
        long after = System.currentTimeMillis();

        assertNotNull(r.getId(), "id must be auto-generated");
        assertDoesNotThrow(() -> UUID.fromString(r.getId()),
                "id must be a valid UUID");

        assertEquals(23.7, r.getValue(), 1e-9);
        assertTrue(r.getTimestamp() >= before && r.getTimestamp() <= after,
                "timestamp must be set to the time of creation");
    }

    @Test
    void distinctReadings_haveDistinctIds() {
        SensorReading a = new SensorReading(1.0);
        SensorReading b = new SensorReading(1.0);
        assertNotEquals(a.getId(), b.getId(),
                "each reading must get a unique UUID, even with the same value");
    }

    @Test
    void defaultCtor_producesEmptyReading() {
        SensorReading r = new SensorReading();
        assertNull(r.getId(), "default ctor must leave id unset for JSON deserialisation");
        assertEquals(0.0, r.getValue(), 1e-9);
        assertEquals(0L, r.getTimestamp());
    }

    @Test
    void settersAllowMutation() {
        SensorReading r = new SensorReading();
        r.setId("custom-id");
        r.setValue(42.0);
        r.setTimestamp(1_700_000_000_000L);

        assertEquals("custom-id", r.getId());
        assertEquals(42.0, r.getValue(), 1e-9);
        assertEquals(1_700_000_000_000L, r.getTimestamp());
    }
}
