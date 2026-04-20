package com.smartcampus.store;

import com.smartcampus.model.Room;
import com.smartcampus.model.Sensor;
import com.smartcampus.model.SensorReading;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DataStoreReadingsTest {

    private final DataStore store = DataStore.getInstance();

    @Test
    void addReading_updatesSensorCurrentValue() {
        store.addRoom(new Room("TEST-RD-R1", "Readings test room", 5));
        store.addSensor(new Sensor("TEST-RD-S1", "Temperature", "TEST-RD-R1"));
        try {
            store.addReading("TEST-RD-S1", new SensorReading(22.5));

            Sensor s = store.getSensor("TEST-RD-S1");
            assertNotNull(s);
            assertEquals(22.5, s.getCurrentValue(), 1e-9,
                    "addReading must update the sensor's currentValue");
        } finally {
            store.removeSensor("TEST-RD-S1");
            store.removeRoom("TEST-RD-R1");
        }
    }

    @Test
    void listReadings_returnsAllAddedReadings() {
        store.addRoom(new Room("TEST-RD-R2", "Readings list test", 5));
        store.addSensor(new Sensor("TEST-RD-S2", "Humidity", "TEST-RD-R2"));
        try {
            store.addReading("TEST-RD-S2", new SensorReading(40.0));
            store.addReading("TEST-RD-S2", new SensorReading(41.5));
            store.addReading("TEST-RD-S2", new SensorReading(42.0));

            List<SensorReading> readings = store.listReadings("TEST-RD-S2");
            assertEquals(3, readings.size());
            assertEquals(40.0, readings.get(0).getValue(), 1e-9);
            assertEquals(42.0, readings.get(2).getValue(), 1e-9);
        } finally {
            store.removeSensor("TEST-RD-S2");
            store.removeRoom("TEST-RD-R2");
        }
    }

    @Test
    void listReadings_unknownSensor_returnsEmptyList() {
        List<SensorReading> readings = store.listReadings("TEST-RD-NOT-A-SENSOR");
        assertNotNull(readings,
                "listReadings must return an empty list, never null");
        assertTrue(readings.isEmpty());
    }

    @Test
    void removeSensor_alsoClearsItsReadings() {
        store.addRoom(new Room("TEST-RD-R3", "Reading cleanup room", 5));
        store.addSensor(new Sensor("TEST-RD-S3", "CO2", "TEST-RD-R3"));
        store.addReading("TEST-RD-S3", new SensorReading(450.0));
        try {
            assertFalse(store.listReadings("TEST-RD-S3").isEmpty());
            store.removeSensor("TEST-RD-S3");
            assertTrue(store.listReadings("TEST-RD-S3").isEmpty(),
                    "removing a sensor must drop its readings too");
        } finally {
            store.removeRoom("TEST-RD-R3");
        }
    }
}
