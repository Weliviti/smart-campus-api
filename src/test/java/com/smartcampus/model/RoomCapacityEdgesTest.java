package com.smartcampus.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Basic sanity tests for Room capacity. These just check that whatever
 * number we put in comes back out. Room has no validation by design —
 * validation lives in the resource layer.
 */
class RoomCapacityEdgesTest {

    @Test
    void capacityCanBeSmall() {
        Room r = new Room("SMALL-1", "One-seater", 1);
        assertEquals(1, r.getCapacity());
    }

    @Test
    void capacityCanBeZero() {
        Room r = new Room("ZERO-1", "Empty room", 0);
        assertEquals(0, r.getCapacity());
    }

    @Test
    void capacityCanBeLarge() {
        Room r = new Room("BIG-1", "Lecture theatre", 500);
        assertEquals(500, r.getCapacity());
    }

    @Test
    void capacityCanBeChangedAfterCreation() {
        Room r = new Room("R-1", "Some room", 10);
        r.setCapacity(25);
        assertEquals(25, r.getCapacity());
    }
}
