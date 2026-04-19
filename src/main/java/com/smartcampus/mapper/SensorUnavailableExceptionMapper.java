package com.smartcampus.mapper;

import com.smartcampus.exception.SensorUnavailableException;
import com.smartcampus.model.ApiError;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/** Converts {@link SensorUnavailableException} into a JSON HTTP 403 Forbidden. */
@Provider
public class SensorUnavailableExceptionMapper
        implements ExceptionMapper<SensorUnavailableException> {

    @Override
    public Response toResponse(SensorUnavailableException ex) {
        ApiError body = new ApiError(
                Response.Status.FORBIDDEN.getStatusCode(),
                "SENSOR_UNAVAILABLE",
                ex.getMessage()
        );
        return Response.status(Response.Status.FORBIDDEN)
                .type(MediaType.APPLICATION_JSON)
                .entity(body)
                .build();
    }
}
