package com.parlAquatics.gateway.simulation.testServer;

import com.parlAquatics.gateway.jetty.cfg.NmsExchangeConfig;
import com.parlAquatics.gateway.protocols.ExchangeHandler;

import java.io.OutputStream;
import java.net.Socket;
import java.util.logging.Logger;

/**
 * Created by Sam Parlatore for ParlAquatics Gateway.
 */
public class TestExchangeHandler implements ExchangeHandler {
    private static final Logger logger = Logger.getLogger(TestExchangeHandler.class.getName());
    private final NmsExchangeConfig config;

    TestExchangeHandler( NmsExchangeConfig config){
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

    @Override
    public void close() {
        ExchangeHandler.super.close();
    }

    @Override
    public void sendRaw(String msg) {

    }

}
