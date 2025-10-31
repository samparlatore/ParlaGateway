package com.parlAquatics.gateway.test.testServer;

import com.parlAquatics.gateway.jetty.cfg.NmsExchangeConfig;
import com.parlAquatics.gateway.protocol.ExchangeHandler;

import java.io.OutputStream;
import java.net.Socket;
import java.util.logging.Logger;

/**
 * Created by Sam Parlatore
 * Part of the ParlAquatics Gateway project
 *
 * This class handles protocol-specific socket processing for exchange connections.
 */
public class DefaultExchangeHandler implements ExchangeHandler {
    private static final Logger logger = Logger.getLogger(DefaultExchangeHandler.class.getName());
    private final NmsExchangeConfig config;

    DefaultExchangeHandler (NmsExchangeConfig config) {
        this.config = config;
    }

    @Override
    public void handle(Socket client) {
        try (OutputStream out = client.getOutputStream()) {
            logger.info("[" + config.getAcronym() + "] Handling client on port " + config.getPort());
            out.write(("OK from " + config.getAcronym() + "\n").getBytes());
            out.flush();
        } catch (Exception e) {
            logger.severe("[" + config.getAcronym() + "] Error responding: " + e.getMessage());
        }
    }
}