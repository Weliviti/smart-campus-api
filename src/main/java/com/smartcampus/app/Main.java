package com.smartcampus.app;

import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.grizzly.http.server.HttpServer;

import java.io.IOException;
import java.net.URI;
import java.util.logging.Logger;

/**
 * Entry point: bootstraps an embedded Grizzly HTTP server hosting the
 * {@link SmartCampusApplication} JAX-RS application.
 */
public final class Main {

    private static final Logger LOG = Logger.getLogger(Main.class.getName());

    private static final String DEFAULT_HOST = "0.0.0.0";
    private static final int DEFAULT_PORT = 8080;

    private Main() {
        // utility class
    }

    public static URI baseUri() {
        String host = System.getProperty("server.host", DEFAULT_HOST);
        int port = Integer.parseInt(System.getProperty("server.port",
                String.valueOf(DEFAULT_PORT)));
        return URI.create("http://" + host + ":" + port + "/");
    }

    public static HttpServer startServer() {
        // Wrap our javax.ws.rs.core.Application subclass for Grizzly/Jersey.
        final ResourceConfig rc = ResourceConfig.forApplication(new SmartCampusApplication());
        return GrizzlyHttpServerFactory.createHttpServer(baseUri(), rc);
    }

    public static void main(String[] args) throws IOException {
        final HttpServer server = startServer();
        LOG.info("Smart Campus API running at " + baseUri() + "api/v1");
        LOG.info("Press Ctrl+C to stop.");

        // Keep the JVM alive until SIGINT
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LOG.info("Shutting down Smart Campus API...");
            server.shutdownNow();
        }));

        // Block main thread
        try {
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
