package com.smartcampus.resource;

import com.smartcampus.exception.SensorUnavailableException;
import com.smartcampus.model.Sensor;
import com.smartcampus.model.SensorReading;
import com.smartcampus.store.DataStore;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.UUID;

/**
 * Handles a single sensor's readings at
 * {@code /api/v1/sensors/{sensorId}/readings}.
 *
 * <p>This class is instantiated per-request by the sub-resource locator in
 * {@link SensorResource#readings(String)}, so the sensor context travels with
 * the instance instead of being repeated in every URL.</p>
 */
@Consumes(MediaType.APPLICATION_JSON)
public class SensorReadingResource {

    private final DataStore store = DataStore.getInstance();
    private final String sensorId;

    public SensorReadingResource(String sensorId) {
        this.sensorId = sensorId;
    }

    /* ----------  GET /sensors/{sensorId}/readings  ---------- */

    @GET
    public List<SensorReading> listReadings() {
        return store.listReadings(sensorId);
    }

    /* ----------  POST /sensors/{sensorId}/readings  ---------- */

    @POST
    public Response addReading(SensorReading reading) {
        if (reading == null) {
            throw new WebApplicationException(
                    "Reading payload is required.", Response.Status.BAD_REQUEST);
        }

        Sensor sensor = store.getSensor(sensorId);
        if (sensor == null) {
            // Shouldn't happen — locator already checked — but guard anyway.
            throw new WebApplicationException(
                    "Sensor '" + sensorId + "' not found.",
                    Response.Status.NOT_FOUND);
        }

        // State-constraint: reject readings on non-active sensors (403).
        if (!sensor.isAvailable()) {
            throw new SensorUnavailableException(sensorId, sensor.getStatus());
        }

        // Fill server-managed fields.
        if (reading.getId() == null || reading.getId().isEmpty()) {
            reading.setId(UUID.randomUUID().toString());
        }
        if (reading.getTimestamp() == 0L) {
            reading.setTimestamp(System.currentTimeMillis());
        }

        store.addReading(sensorId, reading);
        return Response.status(Response.Status.CREATED).entity(reading).build();
    }
}
