package com.smartcampus.mapper;

import com.smartcampus.exception.RoomNotEmptyException;
import com.smartcampus.model.ApiError;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/** Converts {@link RoomNotEmptyException} into a JSON HTTP 409 Conflict. */
@Provider
public class RoomNotEmptyExceptionMapper implements ExceptionMapper<RoomNotEmptyException> {

    @Override
    public Response toResponse(RoomNotEmptyException ex) {
        ApiError body = new ApiError(
                Response.Status.CONFLICT.getStatusCode(),
                "ROOM_NOT_EMPTY",
                ex.getMessage()
        );
        return Response.status(Response.Status.CONFLICT)
                .type(MediaType.APPLICATION_JSON)
                .entity(body)
                .build();
    }
}
