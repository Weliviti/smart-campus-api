package com.smartcampus.exception;

/**
 * Thrown when a reading is posted against a sensor that isn't currently
 * accepting data (status is MAINTENANCE or OFFLINE).
 * Mapped to HTTP 403 Forbidden.
 */
public class SensorUnavailableException extends RuntimeException {

    private final String sensorId;
    private final String status;

    public SensorUnavailableException(String sensorId, String status) {
        super(String.format("Sensor '%s' is not available (status=%s).",
                sensorId, status));
        this.sensorId = sensorId;
        this.status = status;
    }

    public String getSensorId() {
        return sensorId;
    }

    public String getStatus() {
        return status;
    }
}
