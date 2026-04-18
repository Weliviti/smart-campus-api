package com.smartcampus.model;

import java.util.UUID;

/**
 * A single measurement posted by a {@link Sensor} at a given time.
 */
public class SensorReading {

    private String id;
    private long timestamp;
    private double value;

    public SensorReading() {
        // default ctor for JSON deserialization
    }

    public SensorReading(double value) {
        this.id = UUID.randomUUID().toString();
        this.timestamp = System.currentTimeMillis();
        this.value = value;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }
}
