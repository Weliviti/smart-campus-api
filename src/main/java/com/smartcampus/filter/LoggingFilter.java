package com.smartcampus.filter;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;
import java.util.logging.Logger;

/**
 * Cross-cutting request/response logging. Attached to every inbound request
 * by JAX-RS as a {@link Provider}, so individual resource methods don't have
 * to log anything themselves.
 */
@Provider
public class LoggingFilter implements ContainerRequestFilter, ContainerResponseFilter {

    private static final Logger LOG = Logger.getLogger("smart-campus.access");

    @Override
    public void filter(ContainerRequestContext requestCtx) {
        LOG.info(String.format(">> %s %s",
                requestCtx.getMethod(),
                requestCtx.getUriInfo().getRequestUri()));
    }

    @Override
    public void filter(ContainerRequestContext requestCtx,
                       ContainerResponseContext responseCtx) {
        LOG.info(String.format("<< %s %s -> %d",
                requestCtx.getMethod(),
                requestCtx.getUriInfo().getRequestUri(),
                responseCtx.getStatus()));
    }
}
