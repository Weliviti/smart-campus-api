package com.smartcampus.model;

import java.util.ArrayList;
import java.util.List;

/**
 * A physical space on the Smart Campus (e.g. a lecture theatre or lab).
 *
 * <p>Each room has a unique id, a human readable name, a safety capacity,
 * and a list of sensor ids for the hardware deployed inside it.</p>
 */
public class Room {

    private String id;
    private String name;
    private int capacity;
    private List<String> sensorIds = new ArrayList<>();

    public Room() {
        // default ctor required for JSON deserialization
    }

    public Room(String id, String name, int capacity) {
        this.id = id;
        this.name = name;
        this.capacity = capacity;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public List<String> getSensorIds() {
        return sensorIds;
    }

    public void setSensorIds(List<String> sensorIds) {
        this.sensorIds = (sensorIds == null) ? new ArrayList<>() : sensorIds;
    }

    public void addSensorId(String sensorId) {
        if (sensorId != null && !this.sensorIds.contains(sensorId)) {
            this.sensorIds.add(sensorId);
        }
    }

    public void removeSensorId(String sensorId) {
        this.sensorIds.remove(sensorId);
    }
}
