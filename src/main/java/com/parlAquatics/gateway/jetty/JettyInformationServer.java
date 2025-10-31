package com.parlAquatics.gateway.jetty;

import jakarta.servlet.ServletContext;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;

/**
 * Created by Sam Parlatore for ParlAquatics Gateway.
 */
public class JettyInformationServer {
    private static ServletContext servletContext;

    public static void start(int port) throws Exception {
        // Jetty setup...
        // Inside your WebAppContext setup:
        WebAppContext context = new WebAppContext();
        context.setContextPath("/");
        context.setResourceBase("src/main/webapp");
        context.setInitParameter("org.eclipse.jetty.servlet.Default.useFileMappedBuffer", "false");

        // Store the context
        servletContext = context.getServletContext();

        // Start Jetty
        Server server = new Server(port);
        server.setHandler(context);
        server.start();
    }

    public static ServletContext getServletContext() {
        return servletContext;
    }
}