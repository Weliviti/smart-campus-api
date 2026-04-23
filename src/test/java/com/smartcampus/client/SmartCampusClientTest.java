package com.smartcampus.client;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Sanity tests for the console client. We don't spin up a real server
 * here — we just check the client constructs cleanly against different
 * base URLs (default and custom).
 */
class SmartCampusClientTest {

    @Test
    void constructsWithDefaultBaseUrl() {
        SmartCampusClient c = new SmartCampusClient("http://localhost:8080/api/v1");
        assertNotNull(c);
    }

    @Test
    void constructsWithCustomBaseUrl() {
        SmartCampusClient c = new SmartCampusClient("http://example.test:9090/api/v1");
        assertNotNull(c);
    }

    @Test
    void constructsWithTrailingSlash() {
        SmartCampusClient c = new SmartCampusClient("http://localhost:8080/api/v1/");
        assertNotNull(c);
    }
}
