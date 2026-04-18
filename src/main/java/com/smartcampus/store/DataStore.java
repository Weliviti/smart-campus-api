package com.smartcampus.store;

import com.smartcampus.model.Room;
import com.smartcampus.model.Sensor;
import com.smartcampus.model.SensorReading;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Singleton, thread-safe in-memory data store.
 *
 * <p>JAX-RS instantiates a new resource class per request by default, so any
 * shared state has to live somewhere that outlives the request. A singleton
 * backed by {@link ConcurrentHashMap} (for the collections) keeps that simple
 * while remaining safe under concurrent access.</p>
 */
public final class DataStore {

    private static final DataStore INSTANCE = new DataStore();

    private final Map<String, Room> rooms = new ConcurrentHashMap<>();
    private final Map<String, Sensor> sensors = new ConcurrentHashMap<>();
    private final Map<String, List<SensorReading>> readingsBySensor = new ConcurrentHashMap<>();

    private DataStore() {
        seed();
    }

    public static DataStore getInstance() {
        return INSTANCE;
    }

    /** Populate a handful of rooms & sensors so the API is non-empty on boot. */
    private void seed() {
        Room lib = new Room("LIB-301", "Library Quiet Study", 40);
        Room lab = new Room("CS-LAB-1", "Computer Science Lab 1", 25);
        rooms.put(lib.getId(), lib);
        rooms.put(lab.getId(), lab);

        Sensor temp = new Sensor("TEMP-001", "Temperature", lib.getId());
        temp.setCurrentValue(21.5);
        Sensor co2 = new Sensor("CO2-001", "CO2", lib.getId());
        co2.setCurrentValue(480.0);
        Sensor occ = new Sensor("OCC-001", "Occupancy", lab.getId());
        occ.setCurrentValue(12);

        addSensor(temp);
        addSensor(co2);
        addSensor(occ);
    }

    /* =====================  Rooms  ===================== */

    public Collection<Room> listRooms() {
        return rooms.values();
    }

    public Room getRoom(String id) {
        return rooms.get(id);
    }

    public boolean roomExists(String id) {
        return rooms.containsKey(id);
    }

    public void addRoom(Room room) {
        rooms.put(room.getId(), room);
    }

    public Room removeRoom(String id) {
        return rooms.remove(id);
    }

    /* =====================  Sensors  ===================== */

    public Collection<Sensor> listSensors() {
        return sensors.values();
    }

    public Sensor getSensor(String id) {
        return sensors.get(id);
    }

    public boolean sensorExists(String id) {
        return sensors.containsKey(id);
    }

    /** Registers the sensor and wires its id into the parent room's list. */
    public synchronized void addSensor(Sensor sensor) {
        sensors.put(sensor.getId(), sensor);
        readingsBySensor.computeIfAbsent(sensor.getId(), k -> new ArrayList<>());
        Room room = rooms.get(sensor.getRoomId());
        if (room != null) {
            room.addSensorId(sensor.getId());
        }
    }

    public synchronized Sensor removeSensor(String id) {
        Sensor removed = sensors.remove(id);
        readingsBySensor.remove(id);
        if (removed != null) {
            Room room = rooms.get(removed.getRoomId());
            if (room != null) {
                room.removeSensorId(id);
            }
        }
        return removed;
    }

    /* =====================  Readings  ===================== */

    public List<SensorReading> listReadings(String sensorId) {
        return readingsBySensor.getOrDefault(sensorId, new ArrayList<>());
    }

    /**
     * Append a reading and update the parent sensor's {@code currentValue}
     * to keep the top-level object consistent with its history.
     */
    public synchronized void addReading(String sensorId, SensorReading reading) {
        readingsBySensor
                .computeIfAbsent(sensorId, k -> new ArrayList<>())
                .add(reading);
        Sensor s = sensors.get(sensorId);
        if (s != null) {
            s.setCurrentValue(reading.getValue());
        }
    }
}
