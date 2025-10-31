package com.parlAquatics.gateway.jetty.app;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.ServletException;
import java.io.IOException;
import java.util.logging.Logger;
import com.parlAquatics.gateway.core.GatewayCore;

/**
 * Created by Sam Parlatore for ParlAquatics Gateway.
 */
public class MetricsServlet extends HttpServlet {
    private static final Logger logger = Logger.getLogger(MetricsServlet.class.getName());

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(HttpServletResponse.SC_OK);

        String json = String.format(
                "json: %s", "string"
//                "{ \"requests\": %d, \"errors\": %d, \"uptimeMs\": %d }",
//                GatewayCore.getRequestCount(),
//                GatewayCore.getErrorCount(),
//                GatewayCore.getUptimeMs()
        );

        response.getWriter().println(json);
        logger.info("Metrics responded with: " + json);

        String pageTitle = "Gateway Metrics";
        getServletContext().setAttribute("pageTitle", pageTitle);
        request.getRequestDispatcher("/app/metrics.jsp").forward(request, response);
    }
}
