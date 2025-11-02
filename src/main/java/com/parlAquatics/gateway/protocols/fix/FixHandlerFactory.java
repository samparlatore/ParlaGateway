package com.parlAquatics.gateway.protocols.fix;

import com.parlAquatics.gateway.jetty.cfg.NmsExchangeConfig;
import com.parlAquatics.gateway.protocols.ExchangeHandler;
import com.parlAquatics.gateway.protocols.ExchangeHandlerFactory;

/**
 * Factory for creating protocol handlers.
 * Created by Sam Parlatore for ParlAquatics Gateway.
 */
public class FixHandlerFactory implements ExchangeHandlerFactory {

    @Override
    public ExchangeHandler create(NmsExchangeConfig config) {
        return new FixProtocolHandler(config);
    }
}
