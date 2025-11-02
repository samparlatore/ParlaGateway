package com.parlAquatics.gateway.protocols;

import com.parlAquatics.gateway.jetty.cfg.NmsExchangeConfig;

/**
 * Created by Sam Parlatore for ParlAquatics Gateway.
 */
public interface ExchangeHandlerFactory {
    ExchangeHandler create(NmsExchangeConfig config);
}
