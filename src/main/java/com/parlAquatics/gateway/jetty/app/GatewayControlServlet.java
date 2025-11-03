package com.parlAquatics.gateway.jetty.app;

import com.parlAquatics.gateway.core.GatewayCore;

import com.parlAquatics.gateway.jetty.cfg.NmsExchangeConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.ServletException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import jakarta.json.Json;
import jakarta.json.JsonObject;

@WebServlet("/control")
public class GatewayControlServlet extends HttpServlet {
    private static final Logger logger = Logger.getLogger(GatewayControlServlet.class.getName());

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        GatewayCore gateway = (GatewayCore) getServletContext().getAttribute("gatewayCore");
        if (gateway == null) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "GatewayCore not available");
            return;
        }

        String body = new BufferedReader(new InputStreamReader(req.getInputStream())).lines().collect(Collectors.joining("\n"));
        JsonObject json = Json.createReader(new StringReader(body)).readObject();
        String action = json.getString("action");

        // ✅ Handle global actions first
        if ("disconnectAll".equals(action)) {
            logger.info("Action 'disconnectAll' triggered for all exchanges. " + action);
            ExecutorService executor = Executors.newFixedThreadPool(6);
            for (String acronym : gateway.getSessionRegistry().getRegisteredAcronyms()) {
                executor.submit(() -> gateway.disconnect(acronym));
            }
            executor.shutdown(); // allow tasks to finish
            resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
        }

        if ("connectAll".equals(action)) {
            logger.info("Action 'connectAll' triggered for all exchanges. " + action);
            gateway.getExchangeConfigs().forEach(gateway::connect);
            resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
            return;
        }

        // ✅ Then handle per-exchange actions
        String acronym = json.getString("acronym");
        NmsExchangeConfig cfg = gateway.getExchangeConfigByAcronym(acronym);
        if (cfg == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Exchange not found");
            return;
        }

        switch (action) {
            case "disconnect":
                gateway.disconnect(acronym);
                break;
            case "connect":
                gateway.connect(cfg);
                break;
            default:
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unknown action");
                return;
        }

        logger.info("Action '" + action + "' triggered for " + acronym);
        resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
    }
}