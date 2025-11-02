package com.parlAquatics.gateway;

import com.parlAquatics.gateway.core.GatewayCore;
import com.parlAquatics.gateway.util.MicrosecondFormatter;
import com.parlAquatics.gateway.jetty.JettyInformationServer;
import com.parlAquatics.gateway.jetty.cfg.NmsExchangeConfig;
import jakarta.servlet.ServletContext;

import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.ConsoleHandler;
import java.util.logging.Logger;

/**
 * Created by Sam Parlatore for ParlAquatics Gateway.
 */
public class ParlaGateway {

    public static void main(String[] args) throws Exception {
        // Start Jetty health/config server first
        JettyInformationServer.start(8080);

        // Retrieve the context after Jetty starts
        ServletContext context = JettyInformationServer.getServletContext();
        @SuppressWarnings("unchecked")
        ConcurrentHashMap<String, NmsExchangeConfig> exchangeConfigs = (ConcurrentHashMap<String, NmsExchangeConfig>) context.getAttribute("nmsExchangeConfigs");

        @SuppressWarnings("unchecked")
        Properties gatewayConfigs = (Properties) context.getAttribute("generalConfigs");

        Logger rootLogger = Logger.getLogger(""); // root logger
        for (var handler : rootLogger.getHandlers()) {
            if (handler instanceof ConsoleHandler) {
                handler.setFormatter(new MicrosecondFormatter());
            }
        }

        // Optional: disable parent handlers if you want only your format
        rootLogger.setUseParentHandlers(false);


        // Start TCP/IP core with context
        GatewayCore gateway = new GatewayCore(exchangeConfigs, gatewayConfigs);
        context.setAttribute("gatewayCore", gateway);
        new Thread(gateway::start).start();

    }
}