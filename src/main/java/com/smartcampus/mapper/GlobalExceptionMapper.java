package com.smartcampus.mapper;

import com.smartcampus.model.ApiError;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Global safety net. Catches anything no other mapper has handled, logs the
 * stack trace server-side, and returns a clean JSON HTTP 500 to the client.
 *
 * <p>Standard JAX-RS {@link WebApplicationException}s (like 404 Not Found) are
 * re-used as-is so their intended status code is preserved, but the raw stack
 * trace never leaks outside the server.</p>
 */
@Provider
public class GlobalExceptionMapper implements ExceptionMapper<Throwable> {

    private static final Logger LOG = Logger.getLogger(GlobalExceptionMapper.class.getName());

    @Override
    public Response toResponse(Throwable ex) {
        // Let JAX-RS exceptions keep their intended status code.
        if (ex instanceof WebApplicationException) {
            WebApplicationException wae = (WebApplicationException) ex;
            int status = wae.getResponse().getStatus();
            String errCode = (ex instanceof NotFoundException) ? "NOT_FOUND" : "REQUEST_ERROR";
            ApiError body = new ApiError(status, errCode, safeMessage(ex));
            return Response.status(status)
                    .type(MediaType.APPLICATION_JSON)
                    .entity(body)
                    .build();
        }

        // Anything else is unexpected — log the trace, return generic 500.
        LOG.log(Level.SEVERE, "Unhandled exception on API thread", ex);
        ApiError body = new ApiError(
                Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                "INTERNAL_ERROR",
                "An unexpected error occurred. Please contact the administrator."
        );
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .type(MediaType.APPLICATION_JSON)
                .entity(body)
                .build();
    }

    private String safeMessage(Throwable ex) {
        String msg = ex.getMessage();
        return (msg == null || msg.isEmpty()) ? ex.getClass().getSimpleName() : msg;
    }
}
