package com.parlAquatics.gateway;

import com.parlAquatics.gateway.core.GatewayCore;
import com.parlAquatics.gateway.jetty.JettyInformationServer;
import com.parlAquatics.gateway.jetty.cfg.NmsExchangeConfig;
import jakarta.servlet.ServletContext;

import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

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
        ConcurrentHashMap<Integer, NmsExchangeConfig> exchangeConfigs = (ConcurrentHashMap<Integer, NmsExchangeConfig>) context.getAttribute("nmsExchangeConfigs");

        @SuppressWarnings("unchecked")
        Properties gatewayConfigs = (Properties) context.getAttribute("generalConfigs");

        // Start TCP/IP core with context
        new Thread(() -> new GatewayCore(exchangeConfigs, gatewayConfigs).start()).start();

    }
}