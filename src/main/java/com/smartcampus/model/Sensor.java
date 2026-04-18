package com.smartcampus.model;

/**
 * A piece of hardware deployed in a {@link Room}.
 *
 * <p>Status values used by the service:</p>
 * <ul>
 *     <li>{@code ACTIVE}      — fully operational; accepts readings</li>
 *     <li>{@code MAINTENANCE} — temporarily disconnected; rejects readings (HTTP 403)</li>
 *     <li>{@code OFFLINE}     — not reachable; rejects readings (HTTP 403)</li>
 * </ul>
 */
public class Sensor {

    public static final String STATUS_ACTIVE = "ACTIVE";
    public static final String STATUS_MAINTENANCE = "MAINTENANCE";
    public static final String STATUS_OFFLINE = "OFFLINE";

    private String id;
    private String type;
    private String status = STATUS_ACTIVE;
    private double currentValue;
    private String roomId;

    public Sensor() {
        // default ctor for JSON deserialization
    }

    public Sensor(String id, String type, String roomId) {
        this.id = id;
        this.type = type;
        this.roomId = roomId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public double getCurrentValue() {
        return currentValue;
    }

    public void setCurrentValue(double currentValue) {
        this.currentValue = currentValue;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    /** @return {@code true} if the sensor is able to accept new readings. */
    public boolean isAvailable() {
        return STATUS_ACTIVE.equalsIgnoreCase(status);
    }
}
