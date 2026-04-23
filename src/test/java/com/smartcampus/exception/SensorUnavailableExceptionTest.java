package com.smartcampus.exception;

import com.smartcampus.model.Sensor;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Basic tests for SensorUnavailableException. Used when a reading is
 * posted to a sensor that is not ACTIVE. Mapped to HTTP 403.
 */
class SensorUnavailableExceptionTest {

    @Test
    void extendsRuntimeException() {
        SensorUnavailableException ex = new SensorUnavailableException(
                "TEMP-001", Sensor.STATUS_MAINTENANCE);
        assertTrue(ex instanceof RuntimeException);
    }

    @Test
    void storesSensorIdAndStatus() {
        SensorUnavailableException ex = new SensorUnavailableException(
                "TEMP-001", Sensor.STATUS_OFFLINE);
        assertEquals("TEMP-001", ex.getSensorId());
        assertEquals("OFFLINE", ex.getStatus());
    }

    @Test
    void messageMentionsSensorId() {
        SensorUnavailableException ex = new SensorUnavailableException(
                "TEMP-001", Sensor.STATUS_MAINTENANCE);
        assertTrue(ex.getMessage().contains("TEMP-001"));
    }

    @Test
    void messageMentionsStatus() {
        SensorUnavailableException ex = new SensorUnavailableException(
                "TEMP-001", Sensor.STATUS_MAINTENANCE);
        assertTrue(ex.getMessage().contains("MAINTENANCE"));
    }
}
