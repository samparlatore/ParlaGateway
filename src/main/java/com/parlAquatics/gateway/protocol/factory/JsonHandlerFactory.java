package com.parlAquatics.gateway.protocol.factory;

import com.parlAquatics.gateway.jetty.cfg.NmsExchangeConfig;
import com.parlAquatics.gateway.protocol.ExchangeHandler;
import com.parlAquatics.gateway.protocol.JsonProtocolHandler;

/**
 * Factory for creating protocol handlers.
 * Created by Sam Parlatore for ParlAquatics Gateway.
 */
public class JsonHandlerFactory implements ExchangeHandlerFactory{

    @Override
    public ExchangeHandler create(NmsExchangeConfig config) {
        return new JsonProtocolHandler(config);
    }
}
