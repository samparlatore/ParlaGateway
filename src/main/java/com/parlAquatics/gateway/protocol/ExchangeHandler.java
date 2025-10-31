package com.parlAquatics.gateway.protocol;

import java.net.Socket;

/**
 * protocol handlers.
 * Created by Sam Parlatore for ParlAquatics Gateway.
 */
public interface ExchangeHandler {
    void handle(Socket client);
}
