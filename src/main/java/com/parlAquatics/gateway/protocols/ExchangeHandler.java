package com.parlAquatics.gateway.protocols;

import java.net.Socket;

/**
 * protocol handlers.
 * Created by Sam Parlatore for ParlAquatics Gateway.
 */
public interface ExchangeHandler {
    void handle(Socket client);
    default void close() {}
    void sendRaw(String msg);
}
