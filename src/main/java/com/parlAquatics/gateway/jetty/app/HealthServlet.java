package com.parlAquatics.gateway.jetty.app;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.ServletException;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * Created by Sam Parlatore for ParlAquatics Gateway.
 */
public class HealthServlet extends HttpServlet {
    private static final Logger logger = Logger.getLogger(HealthServlet.class.getName());

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Set response headers
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(HttpServletResponse.SC_OK);

        // Write JSON health status
        String json = "{ \"status\": \"OK\", \"uptime\": \"" + System.currentTimeMillis() + "\" }";
        response.getWriter().println(json);
        logger.info("Health check responded with: " + json);

        // Optional: forward to JSP (if you want a visual dashboard)
        String pageTitle = "Gateway Health Status";
        getServletContext().setAttribute("pageTitle", pageTitle);
        request.getRequestDispatcher("/app/health.jsp").forward(request, response);
    }
}
