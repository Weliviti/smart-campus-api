package com.smartcampus.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Sanity checks on SensorReading's value field.
 */
class SensorReadingValueTest {

    @Test
    void valueRoundTripsThroughConstructor() {
        SensorReading r = new SensorReading(42.0);
        assertEquals(42.0, r.getValue(), 0.00001);
    }

    @Test
    void valueCanBeUpdatedAfterCreation() {
        SensorReading r = new SensorReading(1.0);
        r.setValue(2.5);
        assertEquals(2.5, r.getValue(), 0.00001);
    }

    @Test
    void valueCanBeNegative() {
        SensorReading r = new SensorReading(-10.0);
        assertEquals(-10.0, r.getValue(), 0.00001);
    }

    @Test
    void valueCanBeLarge() {
        SensorReading r = new SensorReading(9_999_999.0);
        assertEquals(9_999_999.0, r.getValue(), 0.00001);
    }

    @Test
    void defaultConstructorHasZeroValue() {
        SensorReading r = new SensorReading();
        assertEquals(0.0, r.getValue(), 0.00001);
    }
}
