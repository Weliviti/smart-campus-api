package com.smartcampus.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The currentValue on a sensor is updated whenever a new reading is
 * recorded. This test just confirms the setter/getter work the way we
 * expect.
 */
class SensorCurrentValueTest {

    @Test
    void currentValueStartsAtZero() {
        Sensor s = new Sensor("S1", "Temperature", "R1");
        assertEquals(0.0, s.getCurrentValue(), 0.00001);
    }

    @Test
    void currentValueCanBeUpdated() {
        Sensor s = new Sensor("S1", "Temperature", "R1");
        s.setCurrentValue(21.5);
        assertEquals(21.5, s.getCurrentValue(), 0.00001);
    }

    @Test
    void currentValueCanBeUpdatedMultipleTimes() {
        Sensor s = new Sensor("S1", "Temperature", "R1");
        s.setCurrentValue(21.5);
        s.setCurrentValue(22.7);
        s.setCurrentValue(19.8);
        assertEquals(19.8, s.getCurrentValue(), 0.00001);
    }

    @Test
    void currentValueCanBeNegative() {
        // Temperatures can drop below zero.
        Sensor s = new Sensor("S1", "Temperature", "R1");
        s.setCurrentValue(-5.3);
        assertEquals(-5.3, s.getCurrentValue(), 0.00001);
    }
}
