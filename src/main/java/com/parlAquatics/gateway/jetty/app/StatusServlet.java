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
public class StatusServlet extends HttpServlet {
    private static final Logger logger = Logger.getLogger(StatusServlet.class.getName());

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(HttpServletResponse.SC_OK);

        String json = "{ \"status\": \"Running\", \"threads\": 5, \"port\": 9000 }";
        response.getWriter().println(json);
        logger.info("Status responded with: " + json);

        String pageTitle = "Gateway Runtime Status";
        getServletContext().setAttribute("pageTitle", pageTitle);
        request.getRequestDispatcher("/app/status.jsp").forward(request, response);
    }
}