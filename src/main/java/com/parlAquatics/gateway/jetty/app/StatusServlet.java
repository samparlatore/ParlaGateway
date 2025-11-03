package com.parlAquatics.gateway.jetty.app;

import com.parlAquatics.gateway.core.GatewayCore;
import com.parlAquatics.gateway.core.model.ExchangeStatus;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.ServletException;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Created by Sam Parlatore for ParlAquatics Gateway.
 */
public class StatusServlet extends HttpServlet {
    private static final Logger logger = Logger.getLogger(StatusServlet.class.getName());

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        GatewayCore gateway = (GatewayCore) getServletContext().getAttribute("gatewayCore");
        if (gateway == null) {
            logger.severe("GatewayCore not found in ServletContext");
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "GatewayCore not available");
            return;
        }

        List<ExchangeStatus> statuses = gateway.getExchangeStatuses();

        String json = statuses.stream()
                .map(status -> String.format(
                        "{" +
                                "\"acronym\":\"%s\"," +
                                "\"status\":\"%s\"," +
                                "\"latencyMicros\":%d," +
                                "\"latencyFormatted\":\"%s\"," +
                                "\"lastMessage\":\"%s\"," +
                                "\"lastMessageType\":\"%s\"," +
                                "\"lastMessageAge\":\"%s\"" +
                                "}",
                        escapeJson(status.getAcronym()),
                        escapeJson(status.getStatus()),
                        status.getLatencyMicros(),
                        escapeJson(status.getLatencyFormatted()),
                        escapeJson(status.getLastMessageTimestamp()),
                        escapeJson(status.getLastMessageType()),
                        escapeJson(status.getLastMessageAge())
                ))
                .collect(Collectors.joining(",", "[", "]"));

        request.setAttribute("exchangeStatusJson", json);
        getServletContext().setAttribute("pageTitle", "Exchange Status Dashboard");
        request.getRequestDispatcher("/app/status.jsp").forward(request, response);

        logger.info("Served " + statuses.size() + " exchange statuses to " + request.getRemoteAddr());
    }

    private String escapeJson(String input) {
        if (input == null) return "";
        return input.replace("\"", "\\\"").replace("\n", "").replace("\r", "");
    }
}