package com.smartcampus.mapper;

import com.smartcampus.exception.LinkedResourceNotFoundException;
import com.smartcampus.model.ApiError;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * Converts {@link LinkedResourceNotFoundException} into a JSON
 * HTTP 422 Unprocessable Entity.
 */
@Provider
public class LinkedResourceNotFoundExceptionMapper
        implements ExceptionMapper<LinkedResourceNotFoundException> {

    /** HTTP 422 — standard "Unprocessable Entity". */
    private static final int HTTP_UNPROCESSABLE_ENTITY = 422;

    @Override
    public Response toResponse(LinkedResourceNotFoundException ex) {
        ApiError body = new ApiError(
                HTTP_UNPROCESSABLE_ENTITY,
                "LINKED_RESOURCE_NOT_FOUND",
                ex.getMessage()
        );
        return Response.status(HTTP_UNPROCESSABLE_ENTITY)
                .type(MediaType.APPLICATION_JSON)
                .entity(body)
                .build();
    }
}
