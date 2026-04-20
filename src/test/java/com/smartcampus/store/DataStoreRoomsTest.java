package com.smartcampus.store;

import com.smartcampus.model.Room;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * DataStore is a singleton seeded at JVM startup, so these tests use unique
 * ids (prefix "TEST-RT-") and clean up after themselves rather than wiping
 * the store.
 */
class DataStoreRoomsTest {

    private final DataStore store = DataStore.getInstance();

    @Test
    void addRoom_thenGet_returnsSameInstance() {
        Room input = new Room("TEST-RT-R1", "Unit test room", 10);
        store.addRoom(input);
        try {
            Room found = store.getRoom("TEST-RT-R1");
            assertNotNull(found);
            assertEquals("Unit test room", found.getName());
            assertEquals(10, found.getCapacity());
            assertSame(input, found,
                    "getRoom should return the stored reference");
        } finally {
            store.removeRoom("TEST-RT-R1");
        }
    }

    @Test
    void roomExists_reflectsAddAndRemove() {
        store.addRoom(new Room("TEST-RT-R2", "Existence test", 5));
        assertTrue(store.roomExists("TEST-RT-R2"));
        store.removeRoom("TEST-RT-R2");
        assertFalse(store.roomExists("TEST-RT-R2"));
    }

    @Test
    void getRoom_returnsNullWhenMissing() {
        assertNull(store.getRoom("TEST-RT-DOES-NOT-EXIST"));
    }

    @Test
    void removeRoom_missingReturnsNull() {
        assertNull(store.removeRoom("TEST-RT-DOES-NOT-EXIST"));
    }

    @Test
    void listRooms_includesSeededRooms() {
        assertTrue(store.listRooms().stream()
                        .anyMatch(r -> "LIB-301".equals(r.getId())),
                "seeded room LIB-301 must appear in listRooms()");
    }
}
