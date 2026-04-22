package com.smartcampus.store;

import com.smartcampus.model.Room;
import com.smartcampus.model.Sensor;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The singleton data store seeds a handful of rooms and sensors on JVM
 * startup so the API is non-empty on first boot. These tests pin that
 * contract so a later refactor doesn't silently remove the seed data.
 */
class DataStoreSeedTest {

    private final DataStore store = DataStore.getInstance();

    @Test
    void libraryRoom_isSeeded() {
        Room lib = store.getRoom("LIB-301");
        assertNotNull(lib, "LIB-301 must be seeded on startup");
        assertEquals(40, lib.getCapacity());
    }

    @Test
    void csLabRoom_isSeeded() {
        Room lab = store.getRoom("CS-LAB-1");
        assertNotNull(lab, "CS-LAB-1 must be seeded on startup");
        assertTrue(lab.getCapacity() > 0);
    }

    @Test
    void libraryHasTempAndCo2Sensors() {
        assertTrue(store.sensorExists("TEMP-001"),
                "TEMP-001 must be seeded against LIB-301");
        assertTrue(store.sensorExists("CO2-001"),
                "CO2-001 must be seeded against LIB-301");

        Sensor temp = store.getSensor("TEMP-001");
        assertEquals("LIB-301", temp.getRoomId());
    }

    @Test
    void labHasOccupancySensor() {
        assertTrue(store.sensorExists("OCC-001"),
                "OCC-001 must be seeded against CS-LAB-1");
        Sensor occ = store.getSensor("OCC-001");
        assertEquals("CS-LAB-1", occ.getRoomId());
    }

    @Test
    void seededSensors_areBackReferencedByTheirRoom() {
        Room lib = store.getRoom("LIB-301");
        assertTrue(lib.getSensorIds().contains("TEMP-001"));
        assertTrue(lib.getSensorIds().contains("CO2-001"));
    }
}
