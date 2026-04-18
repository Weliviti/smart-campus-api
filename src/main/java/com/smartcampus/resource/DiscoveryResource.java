package com.smartcampus.resource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Root "Discovery" endpoint.
 *
 * <p>Following the HATEOAS principle the API advertises where its primary
 * collections live; clients don't need to hard-code paths.</p>
 */
@Path("/")
@Produces(MediaType.APPLICATION_JSON)
public class DiscoveryResource {

    public static final String API_VERSION = "1.0.0";

    @GET
    public Response discover(@Context UriInfo uriInfo) {
        String base = uriInfo.getBaseUri().toString();
        // Normalise trailing slash so we don't emit "//rooms"
        if (base.endsWith("/")) {
            base = base.substring(0, base.length() - 1);
        }

        Map<String, String> links = new LinkedHashMap<>();
        links.put("self", base);
        links.put("rooms", base + "/rooms");
        links.put("sensors", base + "/sensors");

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("name", "Smart Campus Sensor & Room Management API");
        body.put("version", API_VERSION);
        body.put("description",
                "REST service for managing Rooms, Sensors and SensorReadings.");
        body.put("contact", contact());
        body.put("links", links);

        return Response.ok(body).build();
    }

    private Map<String, String> contact() {
        Map<String, String> c = new LinkedHashMap<>();
        c.put("name", "Smart Campus Facilities");
        c.put("email", "smart-campus@westminster.ac.uk");
        return c;
    }
}
