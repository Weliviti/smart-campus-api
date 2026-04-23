package com.smartcampus.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Focused tests for the three sensor status values and how isAvailable()
 * reacts to each one.
 */
class SensorStatusValuesTest {

    @Test
    void activeStatus_isAvailable() {
        Sensor s = new Sensor("S1", "Temperature", "R1");
        s.setStatus(Sensor.STATUS_ACTIVE);
        assertEquals("ACTIVE", s.getStatus());
        assertTrue(s.isAvailable());
    }

    @Test
    void maintenanceStatus_isNotAvailable() {
        Sensor s = new Sensor("S2", "CO2", "R1");
        s.setStatus(Sensor.STATUS_MAINTENANCE);
        assertEquals("MAINTENANCE", s.getStatus());
        assertFalse(s.isAvailable());
    }

    @Test
    void offlineStatus_isNotAvailable() {
        Sensor s = new Sensor("S3", "Occupancy", "R1");
        s.setStatus(Sensor.STATUS_OFFLINE);
        assertEquals("OFFLINE", s.getStatus());
        assertFalse(s.isAvailable());
    }

    @Test
    void unknownStatus_isNotAvailable() {
        // If someone sets a weird value, isAvailable should say no rather
        // than accidentally accept readings.
        Sensor s = new Sensor("S4", "Humidity", "R1");
        s.setStatus("BROKEN");
        assertFalse(s.isAvailable());
    }
}
