package com.smartcampus.exception;

/**
 * Thrown when a request body references a related resource that does not
 * exist (e.g. a Sensor pointing at an unknown {@code roomId}).
 * Mapped to HTTP 422 Unprocessable Entity.
 */
public class LinkedResourceNotFoundException extends RuntimeException {

    private final String linkedType;
    private final String linkedId;

    public LinkedResourceNotFoundException(String linkedType, String linkedId) {
        super(String.format("Referenced %s with id '%s' does not exist.",
                linkedType, linkedId));
        this.linkedType = linkedType;
        this.linkedId = linkedId;
    }

    public String getLinkedType() {
        return linkedType;
    }

    public String getLinkedId() {
        return linkedId;
    }
}
