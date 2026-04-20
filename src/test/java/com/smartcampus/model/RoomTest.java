package com.smartcampus.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RoomTest {

    @Test
    void newRoom_hasEmptySensorList() {
        Room r = new Room("LIB-301", "Main Library 301", 40);
        assertNotNull(r.getSensorIds(), "sensorIds must never be null");
        assertTrue(r.getSensorIds().isEmpty(), "new room starts with no sensors");
    }

    @Test
    void gettersReturnConstructorValues() {
        Room r = new Room("CS-LAB-1", "CS Lab 1", 30);
        assertEquals("CS-LAB-1", r.getId());
        assertEquals("CS Lab 1", r.getName());
        assertEquals(30, r.getCapacity());
    }

    @Test
    void addSensorId_addsOnce() {
        Room r = new Room("LIB-301", "Main Library 301", 40);
        r.addSensorId("TEMP-001");
        r.addSensorId("TEMP-001"); // idempotent
        assertEquals(1, r.getSensorIds().size(),
                "duplicate sensor ids must not accumulate");
        assertTrue(r.getSensorIds().contains("TEMP-001"));
    }

    @Test
    void addSensorId_nullIsIgnored() {
        Room r = new Room("LIB-301", "Main Library 301", 40);
        assertDoesNotThrow(() -> r.addSensorId(null));
        assertTrue(r.getSensorIds().isEmpty(),
                "null sensor ids must be silently ignored");
    }

    @Test
    void removeSensorId_removesIfPresent() {
        Room r = new Room("LIB-301", "Main Library 301", 40);
        r.addSensorId("TEMP-001");
        r.addSensorId("CO2-001");

        r.removeSensorId("TEMP-001");

        assertEquals(1, r.getSensorIds().size());
        assertFalse(r.getSensorIds().contains("TEMP-001"));
        assertTrue(r.getSensorIds().contains("CO2-001"));
    }

    @Test
    void removeSensorId_missingIsNoOp() {
        Room r = new Room("LIB-301", "Main Library 301", 40);
        assertDoesNotThrow(() -> r.removeSensorId("GHOST-999"));
    }

    @Test
    void setSensorIds_nullReplacedWithEmpty() {
        Room r = new Room("LIB-301", "Main Library 301", 40);
        r.setSensorIds(null);
        assertNotNull(r.getSensorIds());
        assertTrue(r.getSensorIds().isEmpty());
    }

    @Test
    void setters_roundTrip() {
        Room r = new Room();
        r.setId("X1");
        r.setName("Room X1");
        r.setCapacity(12);
        assertEquals("X1", r.getId());
        assertEquals("Room X1", r.getName());
        assertEquals(12, r.getCapacity());
    }
}
