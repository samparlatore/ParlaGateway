package com.parlAquatics.gateway.protocols.json;

import com.parlAquatics.gateway.jetty.cfg.NmsExchangeConfig;
import com.parlAquatics.gateway.protocols.ExchangeHandler;

import java.net.Socket;

/**
 * Created by Sam Parlatore
 * Part of the ParlAquatics Gateway project
 *
 * This class handles protocol-specific socket processing for exchange connections.
 */
public class JsonProtocolHandler implements ExchangeHandler {
    private final NmsExchangeConfig config;

    public JsonProtocolHandler(NmsExchangeConfig config) {
        this.config = config;
    }

    public void handle(Socket client) {
        // Use this.config inside
    }

    @Override
    public void close() {
        ExchangeHandler.super.close();
    }

    @Override
    public void sendRaw(String msg) {

    }


}
