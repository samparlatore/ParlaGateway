package com.parlAquatics.gateway.protocol;

import com.parlAquatics.gateway.jetty.cfg.NmsExchangeConfig;

import java.net.Socket;

/**
 * Created by Sam Parlatore
 * Part of the ParlAquatics Gateway project
 *
 * This class handles protocol-specific socket processing for exchange connections.
 */
public class FixProtocolHandler implements ExchangeHandler {
    private final NmsExchangeConfig config;

    public FixProtocolHandler(NmsExchangeConfig config) {
        this.config = config;
    }

    public void handle(Socket client) {
        // Use this.config inside
    }
}
//private final FixSessionFactory sessionFactory;
//
//public FixProtocolHandler(FixSessionFactory sessionFactory) {
//    this.sessionFactory = sessionFactory;
//}
//
//public void handle(Socket client, NmsExchangeConfig config) {
//    FixSession session = sessionFactory.createSession(client, config);
//    session.start(); // internally managed threads
//}
