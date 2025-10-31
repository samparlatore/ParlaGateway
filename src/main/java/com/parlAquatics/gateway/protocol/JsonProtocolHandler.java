package com.parlAquatics.gateway.protocol;

import com.parlAquatics.gateway.jetty.cfg.NmsExchangeConfig;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
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
}
