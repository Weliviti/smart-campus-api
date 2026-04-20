package com.smartcampus.store;

import com.smartcampus.model.Room;
import com.smartcampus.model.Sensor;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DataStoreSensorsTest {

    private final DataStore store = DataStore.getInstance();

    @Test
    void addSensor_linksBackToRoomSensorIds() {
        store.addRoom(new Room("TEST-ST-R1", "Room for sensor linking", 5));
        store.addSensor(new Sensor("TEST-ST-S1", "CO2", "TEST-ST-R1"));
        try {
            Room r = store.getRoom("TEST-ST-R1");
            assertNotNull(r);
            assertTrue(r.getSensorIds().contains("TEST-ST-S1"),
                    "room must back-reference its new sensor");
        } finally {
            store.removeSensor("TEST-ST-S1");
            store.removeRoom("TEST-ST-R1");
        }
    }

    @Test
    void addSensor_withUnknownRoom_stillPersistsSensor() {
        // DataStore itself does not validate roomId — that's the resource
        // layer's job. The store should just not NPE.
        store.addSensor(new Sensor("TEST-ST-ORPHAN", "Temperature", "TEST-ST-NO-ROOM"));
        try {
            assertTrue(store.sensorExists("TEST-ST-ORPHAN"));
        } finally {
            store.removeSensor("TEST-ST-ORPHAN");
        }
    }

    @Test
    void listSensors_containsEverySensorAdded() {
        store.addRoom(new Room("TEST-ST-R3", "Multi-sensor room", 5));
        store.addSensor(new Sensor("TEST-ST-T1", "Temperature", "TEST-ST-R3"));
        store.addSensor(new Sensor("TEST-ST-T2", "Temperature", "TEST-ST-R3"));
        store.addSensor(new Sensor("TEST-ST-C1", "CO2",         "TEST-ST-R3"));
        try {
            assertTrue(store.listSensors().stream()
                    .anyMatch(s -> "TEST-ST-T1".equals(s.getId())));
            assertTrue(store.listSensors().stream()
                    .anyMatch(s -> "TEST-ST-T2".equals(s.getId())));
            assertTrue(store.listSensors().stream()
                    .anyMatch(s -> "TEST-ST-C1".equals(s.getId())));
        } finally {
            store.removeSensor("TEST-ST-T1");
            store.removeSensor("TEST-ST-T2");
            store.removeSensor("TEST-ST-C1");
            store.removeRoom("TEST-ST-R3");
        }
    }

    @Test
    void removeSensor_removesBackReferenceFromRoom() {
        store.addRoom(new Room("TEST-ST-R4", "For back-ref test", 5));
        store.addSensor(new Sensor("TEST-ST-S4", "Humidity", "TEST-ST-R4"));
        try {
            Sensor removed = store.removeSensor("TEST-ST-S4");
            assertNotNull(removed);

            Room r = store.getRoom("TEST-ST-R4");
            assertNotNull(r);
            assertFalse(r.getSensorIds().contains("TEST-ST-S4"),
                    "removing a sensor must drop it from its room's sensorIds");
        } finally {
            store.removeRoom("TEST-ST-R4");
        }
    }

    @Test
    void removeSensor_missingReturnsNull() {
        assertNull(store.removeSensor("TEST-ST-NOT-THERE"));
    }
}
