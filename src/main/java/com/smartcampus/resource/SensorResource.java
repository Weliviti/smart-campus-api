package com.smartcampus.resource;

import com.smartcampus.exception.LinkedResourceNotFoundException;
import com.smartcampus.model.Sensor;
import com.smartcampus.store.DataStore;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Manages sensors at {@code /api/v1/sensors}.
 *
 * <p>POST validates that the referenced {@code roomId} exists. Supports an
 * optional {@code ?type=} query parameter on GET for filtering.</p>
 */
@Path("/sensors")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorResource {

    private final DataStore store = DataStore.getInstance();

    /* ----------  GET /sensors  (optional ?type=) ---------- */

    @GET
    public List<Sensor> listSensors(@QueryParam("type") String type) {
        List<Sensor> all = store.listSensors().stream().collect(Collectors.toList());
        if (type == null || type.isEmpty()) {
            return all;
        }
        return all.stream()
                .filter(s -> type.equalsIgnoreCase(s.getType()))
                .collect(Collectors.toList());
    }

    /* ----------  GET /sensors/{sensorId}  ---------- */

    @GET
    @Path("/{sensorId}")
    public Sensor getSensor(@PathParam("sensorId") String sensorId) {
        Sensor s = store.getSensor(sensorId);
        if (s == null) {
            throw new NotFoundException("Sensor '" + sensorId + "' not found.");
        }
        return s;
    }

    /* ----------  POST /sensors  ---------- */

    @POST
    public Response createSensor(Sensor sensor, @Context UriInfo uriInfo) {
        if (sensor == null || sensor.getId() == null || sensor.getId().isEmpty()) {
            throw new WebApplicationException(
                    "Sensor id is required.", Response.Status.BAD_REQUEST);
        }
        if (sensor.getRoomId() == null || sensor.getRoomId().isEmpty()) {
            throw new WebApplicationException(
                    "Sensor.roomId is required.", Response.Status.BAD_REQUEST);
        }
        if (!store.roomExists(sensor.getRoomId())) {
            // 422 Unprocessable Entity — syntax is valid, the reference isn't.
            throw new LinkedResourceNotFoundException("Room", sensor.getRoomId());
        }
        if (store.sensorExists(sensor.getId())) {
            throw new WebApplicationException(
                    "Sensor '" + sensor.getId() + "' already exists.",
                    Response.Status.CONFLICT);
        }
        store.addSensor(sensor);
        URI location = uriInfo.getAbsolutePathBuilder()
                .path(sensor.getId())
                .build();
        return Response.created(location).entity(sensor).build();
    }

    /* ----------  DELETE /sensors/{sensorId}  ---------- */

    @DELETE
    @Path("/{sensorId}")
    public Response deleteSensor(@PathParam("sensorId") String sensorId) {
        Sensor removed = store.removeSensor(sensorId);
        if (removed == null) {
            throw new NotFoundException("Sensor '" + sensorId + "' not found.");
        }
        return Response.noContent().build();
    }

    /* ----------  Sub-resource locator: /sensors/{sensorId}/readings  ---------- */

    @Path("/{sensorId}/readings")
    public SensorReadingResource readings(@PathParam("sensorId") String sensorId) {
        if (!store.sensorExists(sensorId)) {
            throw new NotFoundException("Sensor '" + sensorId + "' not found.");
        }
        return new SensorReadingResource(sensorId);
    }
}
