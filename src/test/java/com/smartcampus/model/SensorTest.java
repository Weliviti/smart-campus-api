package com.smartcampus.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SensorTest {

    @Test
    void statusConstants_areCanonicalStrings() {
        assertEquals("ACTIVE",      Sensor.STATUS_ACTIVE);
        assertEquals("MAINTENANCE", Sensor.STATUS_MAINTENANCE);
        assertEquals("OFFLINE",     Sensor.STATUS_OFFLINE);
    }

    @Test
    void defaultsToActive_whenStatusNotSet() {
        Sensor s = new Sensor("TEMP-001", "Temperature", "LIB-301");
        assertEquals(Sensor.STATUS_ACTIVE, s.getStatus(),
                "a freshly created sensor should be ACTIVE by default");
        assertTrue(s.isAvailable(),
                "ACTIVE sensor must report isAvailable() == true");
    }

    @Test
    void isAvailable_falseForNonActiveStatus() {
        Sensor s = new Sensor("TEMP-001", "Temperature", "LIB-301");

        s.setStatus(Sensor.STATUS_MAINTENANCE);
        assertFalse(s.isAvailable(), "MAINTENANCE must not be available");

        s.setStatus(Sensor.STATUS_OFFLINE);
        assertFalse(s.isAvailable(), "OFFLINE must not be available");
    }

    @Test
    void isAvailable_isCaseInsensitive() {
        Sensor s = new Sensor("TEMP-001", "Temperature", "LIB-301");
        s.setStatus("active");
        assertTrue(s.isAvailable(),
                "isAvailable() must be case insensitive for robustness");
    }

    @Test
    void currentValue_defaultsToZero_thenRoundTrips() {
        Sensor s = new Sensor("TEMP-001", "Temperature", "LIB-301");
        assertEquals(0.0, s.getCurrentValue(), 1e-9,
                "currentValue must default to 0.0 for a fresh sensor");
        s.setCurrentValue(21.4);
        assertEquals(21.4, s.getCurrentValue(), 1e-9);
    }

    @Test
    void gettersRoundTripConstructorValues() {
        Sensor s = new Sensor("CO2-001", "CO2", "LIB-301");
        assertEquals("CO2-001", s.getId());
        assertEquals("CO2", s.getType());
        assertEquals("LIB-301", s.getRoomId());
    }

    @Test
    void setters_roundTrip() {
        Sensor s = new Sensor();
        s.setId("X");
        s.setType("Humidity");
        s.setRoomId("R");
        assertEquals("X", s.getId());
        assertEquals("Humidity", s.getType());
        assertEquals("R", s.getRoomId());
    }
}
