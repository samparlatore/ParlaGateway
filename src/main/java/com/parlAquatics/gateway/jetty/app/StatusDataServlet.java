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
public class StatusDataServlet extends HttpServlet {
    private static final Logger logger = Logger.getLogger(StatusDataServlet.class.getName());

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        GatewayCore gateway = (GatewayCore) getServletContext().getAttribute("gatewayCore");
        if (gateway == null) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "GatewayCore not available");
            return;
        }

        List<ExchangeStatus> statuses = gateway.getExchangeStatuses();

        String json = statuses.stream()
                .map(status -> String.format(
                        "{\"acronym\":\"%s\",\"status\":\"%s\",\"latency\":%d,\"lastMessage\":\"%s\"}",
                        escapeJson(status.getAcronym()),
                        escapeJson(status.getStatus()),
                        status.getLatencyMs(),
                        escapeJson(status.getLastMessageTimestamp())
                ))
                .collect(Collectors.joining(",", "[", "]"));

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(json);

        //logger.info("Delivered " + statuses.size() + " statuses to " + request.getRemoteAddr());
    }

    private String escapeJson(String input) {
        if (input == null) return "";
        return input.replace("\"", "\\\"").replace("\n", "").replace("\r", "");
    }
}