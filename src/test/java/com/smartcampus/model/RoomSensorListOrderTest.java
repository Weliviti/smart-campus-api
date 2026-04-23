package com.smartcampus.model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The sensor id list on a room should keep the order in which ids were
 * added. This matters because clients may rely on that order for
 * display.
 */
class RoomSensorListOrderTest {

    @Test
    void sensorsKeepInsertionOrder() {
        Room r = new Room("R1", "Room", 10);
        r.addSensorId("A");
        r.addSensorId("B");
        r.addSensorId("C");

        List<String> ids = r.getSensorIds();
        assertEquals(3, ids.size());
        assertEquals("A", ids.get(0));
        assertEquals("B", ids.get(1));
        assertEquals("C", ids.get(2));
    }

    @Test
    void removingOneDoesNotChangeTheRestsOrder() {
        Room r = new Room("R1", "Room", 10);
        r.addSensorId("A");
        r.addSensorId("B");
        r.addSensorId("C");
        r.removeSensorId("B");

        List<String> ids = r.getSensorIds();
        assertEquals(2, ids.size());
        assertEquals("A", ids.get(0));
        assertEquals("C", ids.get(1));
    }

    @Test
    void addingAfterRemoveAppendsToTheEnd() {
        Room r = new Room("R1", "Room", 10);
        r.addSensorId("A");
        r.addSensorId("B");
        r.removeSensorId("A");
        r.addSensorId("C");

        List<String> ids = r.getSensorIds();
        assertEquals(2, ids.size());
        assertEquals("B", ids.get(0));
        assertEquals("C", ids.get(1));
    }
}
