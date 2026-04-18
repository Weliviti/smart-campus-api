package com.smartcampus.model;

/**
 * Standard JSON shape returned to clients whenever an exception is mapped.
 * Keeps error payloads uniform across the API.
 */
public class ApiError {

    private int status;
    private String error;
    private String message;
    private long timestamp = System.currentTimeMillis();

    public ApiError() {
        // default ctor for JSON serialization
    }

    public ApiError(int status, String error, String message) {
        this.status = status;
        this.error = error;
        this.message = message;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
