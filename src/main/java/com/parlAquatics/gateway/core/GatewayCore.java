package com.parlAquatics.gateway.core;

import com.parlAquatics.gateway.core.util.MicrosecondFormatter;
import com.parlAquatics.gateway.jetty.cfg.NmsExchangeConfig;
import com.parlAquatics.gateway.protocol.ExchangeHandler;
import com.parlAquatics.gateway.protocol.factory.ExchangeHandlerFactory;
import com.parlAquatics.gateway.protocol.FixProtocolHandler;
import com.parlAquatics.gateway.protocol.JsonProtocolHandler;
import com.parlAquatics.gateway.protocol.factory.FixHandlerFactory;
import com.parlAquatics.gateway.protocol.factory.JsonHandlerFactory;

import java.io.IOException;
import java.net.Socket;
import java.util.Properties;
import java.util.concurrent.*;
import java.util.logging.ConsoleHandler;
import java.util.logging.Logger;

/**
 * Created by Sam Parlatore
 * Part of the ParlAquatics Gateway project
 *
 */
public class GatewayCore {
    private final ExecutorService executor;
    private static final Logger logger = Logger.getLogger(GatewayCore.class.getName());
    private static int requestCount = 0;
    private static int errorCount = 0;
    private static long startTime = System.currentTimeMillis();

    private final ConcurrentHashMap<Integer, NmsExchangeConfig> exchangeConfigs;
    private final Properties configs;
    private final ConcurrentHashMap<String, ExchangeHandlerFactory> handlerFactories = new ConcurrentHashMap<>();


    public GatewayCore(ConcurrentHashMap<Integer, NmsExchangeConfig> exchangeConfigs, Properties configs) {
        this.exchangeConfigs = exchangeConfigs;
        this.configs = configs;
        handlerFactories.put("fix", new FixHandlerFactory());
        handlerFactories.put("json", new JsonHandlerFactory());
        String model = configs.getProperty("gateway.threadingModel", "fixed");
        int threadCount = Integer.parseInt(configs.getProperty("gateway.threadPoolSize", "8"));
        switch (model) {
            case "cached":
                this.executor = Executors.newCachedThreadPool();
                break;
            case "scheduled":
                this.executor = Executors.newScheduledThreadPool(threadCount);
                break;
            case "workStealing":
                this.executor = Executors.newWorkStealingPool(threadCount);
                break;
            default:
                this.executor = Executors.newFixedThreadPool(threadCount);
                break;
        }
        logger.info("Initialized thread pool with model: " + model + ", size: " + threadCount);
    }

    public void start() {
        logger.info("GatewayCore starting with " + exchangeConfigs.size() + " exchanges.");

        for (NmsExchangeConfig exConfig : exchangeConfigs.values()) {
            executor.submit(() -> connectToExchange(exConfig, configs));
        }
    }

    private void connectToExchange(NmsExchangeConfig exchangeConfig, Properties config) {
        int attempts = 0;
        boolean connected = false;
        String backoffStrategy = config.getProperty("gateway.retryBackoffStrategy", "none");

        while (attempts < exchangeConfig.getConnectionRetries() && !connected) {
            try {
                logger.info("[" + exchangeConfig.getAcronym() + "] Connecting to " +
                        exchangeConfig.getIpAddress() + ":" + exchangeConfig.getPort() +
                        " (Attempt " + (attempts + 1) + ")");

                Socket socket = new Socket(exchangeConfig.getIpAddress(), exchangeConfig.getPort());
                connected = true;
                requestCount++;

                ExchangeHandlerFactory factory = handlerFactories.get(exchangeConfig.getHandlerName());
                ExchangeHandler handler = (factory != null) ? factory.create(exchangeConfig) : null;
                if (handler != null) {
                    handler.handle(socket);
                } else {
                    logger.warning("Unknown handler: " + exchangeConfig.getHandlerName());
                    socket.close();
                }
                logger.info("[" + exchangeConfig.getAcronym() + "] Connected successfully.");
                socket.close();

            } catch (IOException e) {
                attempts++;
                errorCount++;
                logger.warning("[" + exchangeConfig.getAcronym() + "] Connection failed: " + e.getMessage());

                long delay = calculateBackoff(backoffStrategy, attempts, exchangeConfig.getConnectionRetryInterval());
                delay += ThreadLocalRandom.current().nextInt(0, 500); // adds up to 500ms jitter so retrys don't syncronize
                logger.info("[" + exchangeConfig.getAcronym() + "] Waiting " + delay + "ms before retry...");

                try {
                    Thread.sleep(delay);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    logger.warning("Retry sleep interrupted for " + exchangeConfig.getAcronym());
                    break;
                }
            }
        }

        if (!connected) {
            logger.severe("[" + exchangeConfig.getAcronym() + "] All connection attempts failed.");
            // TODO: Apply recovery behavior
        }
    }

    public void shutdown() {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    private long calculateBackoff(String strategy, int attempt, int baseInterval) {
        switch (strategy.toLowerCase()) {
            case "exponential":
                return baseInterval * (1L << attempt); // 2^attempt
            case "linear":
                return baseInterval * attempt;
            default:
                return baseInterval; // "none" or unrecognized
        }
    }
}