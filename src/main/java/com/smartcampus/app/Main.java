package com.smartcampus.app;

import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.Wrapper;
import org.apache.catalina.startup.Tomcat;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;

import java.io.File;
import java.net.URI;
import java.util.logging.Logger;

/**
 * Entry point: bootstraps an embedded Tomcat 9 server hosting the
 * {@link SmartCampusApplication} JAX-RS application via Jersey's
 * ServletContainer. Jersey is mounted at /api/v1/*.
 */
public final class Main {

    private static final Logger LOG = Logger.getLogger(Main.class.getName());

    private static final String DEFAULT_HOST = "0.0.0.0";
    private static final int DEFAULT_PORT = 8080;
    private static final String API_MAPPING = "/api/v1/*";

    private Main() {
        // utility class
    }

    public static URI baseUri() {
        String host = System.getProperty("server.host", DEFAULT_HOST);
        int port = Integer.parseInt(System.getProperty("server.port",
                String.valueOf(DEFAULT_PORT)));
        return URI.create("http://" + host + ":" + port + "/");
    }

    public static Tomcat startServer() throws LifecycleException {
        String host = System.getProperty("server.host", DEFAULT_HOST);
        int port = Integer.parseInt(System.getProperty("server.port",
                String.valueOf(DEFAULT_PORT)));

        Tomcat tomcat = new Tomcat();
        tomcat.setHostname(host);
        tomcat.setPort(port);
        // Force connector creation so Tomcat actually listens on the port.
        tomcat.getConnector();

        // Tomcat needs a "base dir" to write work files into. Using the
        // system tmp directory keeps the app portable.
        File baseDir = new File(System.getProperty("java.io.tmpdir"));
        Context ctx = tomcat.addContext("", baseDir.getAbsolutePath());

        // Wrap our JAX-RS Application in Jersey's ServletContainer, then
        // register that servlet with Tomcat and map it under /api/v1/*.
        ResourceConfig config = ResourceConfig.forApplication(new SmartCampusApplication());
        ServletContainer jerseyServlet = new ServletContainer(config);

        Wrapper wrapper = Tomcat.addServlet(ctx, "jersey", jerseyServlet);
        wrapper.setLoadOnStartup(1);
        wrapper.addMapping(API_MAPPING);

        tomcat.start();
        return tomcat;
    }

    public static void main(String[] args) throws LifecycleException {
        final Tomcat server = startServer();
        LOG.info("Smart Campus API running at " + baseUri() + "api/v1");
        LOG.info("Press Ctrl+C to stop.");

        // Stop Tomcat cleanly on SIGINT.
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LOG.info("Shutting down Smart Campus API...");
            try {
                server.stop();
                server.destroy();
            } catch (LifecycleException e) {
                LOG.warning("Error during shutdown: " + e.getMessage());
            }
        }));

        // Block main thread on Tomcat's server await loop.
        server.getServer().await();
    }
}
