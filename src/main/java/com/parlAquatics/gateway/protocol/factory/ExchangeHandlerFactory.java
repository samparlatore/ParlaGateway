package com.parlAquatics.gateway.protocol.factory;

import com.parlAquatics.gateway.jetty.cfg.NmsExchangeConfig;
import com.parlAquatics.gateway.protocol.ExchangeHandler;

/**
 * Created by Sam Parlatore for ParlAquatics Gateway.
 */
public interface ExchangeHandlerFactory {
    ExchangeHandler create(NmsExchangeConfig config);
}
