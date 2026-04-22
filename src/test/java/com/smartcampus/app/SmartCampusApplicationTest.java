package com.smartcampus.app;

import com.smartcampus.filter.LoggingFilter;
import com.smartcampus.mapper.GlobalExceptionMapper;
import com.smartcampus.mapper.LinkedResourceNotFoundExceptionMapper;
import com.smartcampus.mapper.RoomNotEmptyExceptionMapper;
import com.smartcampus.mapper.SensorUnavailableExceptionMapper;
import com.smartcampus.resource.DiscoveryResource;
import com.smartcampus.resource.SensorResource;
import com.smartcampus.resource.SensorRoom;
import org.junit.jupiter.api.Test;

import javax.ws.rs.ApplicationPath;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class SmartCampusApplicationTest {

    @Test
    void applicationPath_isVersionedBase() {
        ApplicationPath ann = SmartCampusApplication.class
                .getAnnotation(ApplicationPath.class);
        assertNotNull(ann, "@ApplicationPath must be present");
        assertEquals("/api/v1", ann.value(),
                "every resource must live under /api/v1");
    }

    @Test
    void getClasses_registersAllResources() {
        Set<Class<?>> classes = new SmartCampusApplication().getClasses();
        assertTrue(classes.contains(DiscoveryResource.class));
        assertTrue(classes.contains(SensorRoom.class));
        assertTrue(classes.contains(SensorResource.class));
    }

    @Test
    void getClasses_registersAllExceptionMappers() {
        Set<Class<?>> classes = new SmartCampusApplication().getClasses();
        assertTrue(classes.contains(RoomNotEmptyExceptionMapper.class));
        assertTrue(classes.contains(LinkedResourceNotFoundExceptionMapper.class));
        assertTrue(classes.contains(SensorUnavailableExceptionMapper.class));
        assertTrue(classes.contains(GlobalExceptionMapper.class),
                "500 safety-net mapper must be registered");
    }

    @Test
    void getClasses_registersLoggingFilter() {
        Set<Class<?>> classes = new SmartCampusApplication().getClasses();
        assertTrue(classes.contains(LoggingFilter.class),
                "request/response logging filter must be registered");
    }

    @Test
    void getClasses_isNotEmpty() {
        assertFalse(new SmartCampusApplication().getClasses().isEmpty());
    }
}
