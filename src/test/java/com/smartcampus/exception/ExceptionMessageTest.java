package com.smartcampus.exception;

import com.smartcampus.model.Sensor;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ExceptionMessageTest {

    @Test
    void roomNotEmpty_carriesRoomIdAndCount() {
        RoomNotEmptyException ex = new RoomNotEmptyException("LIB-301", 3);
        assertEquals("LIB-301", ex.getRoomId());
        assertEquals(3, ex.getSensorCount());
        assertNotNull(ex.getMessage());
        assertTrue(ex.getMessage().contains("LIB-301"),
                "message should include the offending room id");
    }

    @Test
    void linkedResourceNotFound_carriesTypeAndId() {
        LinkedResourceNotFoundException ex =
                new LinkedResourceNotFoundException("Room", "DOES-NOT-EXIST");
        assertEquals("Room", ex.getLinkedType());
        assertEquals("DOES-NOT-EXIST", ex.getLinkedId());
        assertTrue(ex.getMessage().contains("DOES-NOT-EXIST"));
    }

    @Test
    void sensorUnavailable_carriesSensorIdAndStatus() {
        SensorUnavailableException ex = new SensorUnavailableException(
                "TEMP-001", Sensor.STATUS_MAINTENANCE);
        assertEquals("TEMP-001", ex.getSensorId());
        assertEquals(Sensor.STATUS_MAINTENANCE, ex.getStatus());
        assertTrue(ex.getMessage().contains("TEMP-001"));
        assertTrue(ex.getMessage().contains(Sensor.STATUS_MAINTENANCE));
    }
}
