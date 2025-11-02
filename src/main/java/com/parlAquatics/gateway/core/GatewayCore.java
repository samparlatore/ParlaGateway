package com.parlAquatics.gateway.core;

import com.parlAquatics.gateway.core.model.ExchangeStatus;
import com.parlAquatics.gateway.jetty.cfg.NmsExchangeConfig;
import com.parlAquatics.gateway.protocols.ExchangeHandler;
import com.parlAquatics.gateway.protocols.ExchangeHandlerFactory;
import com.parlAquatics.gateway.protocols.fix.FixHandlerFactory;
import com.parlAquatics.gateway.protocols.fix.FixMessageBuilder;
import com.parlAquatics.gateway.protocols.fix.FixProtocolHandler;
import com.parlAquatics.gateway.protocols.fix.FixSessionManager;
import com.parlAquatics.gateway.protocols.json.JsonHandlerFactory;

import java.io.IOException;
import java.net.Socket;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Created by Sam Parlatore
 * Part of the ParlAquatics Gateway project
 */
public class GatewayCore {
    private static final Logger logger = Logger.getLogger(GatewayCore.class.getName());
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final ExecutorService executor;
    private static int requestCount = 0;
    private static int errorCount = 0;
    private static long startTime = System.currentTimeMillis();
    private LocalTime endOfDayTime;


    private final Properties configs;
    private final Map<String, NmsExchangeConfig> exchangeConfigs;
    private final Map<String, ExchangeHandlerFactory> handlerFactories = new ConcurrentHashMap<>();
    private final Map<String, ExchangeHandler> activeHandlers = new ConcurrentHashMap<>();
    private final Map<String, Future<?>> activeConnections = new ConcurrentHashMap<>();

    public Iterable<NmsExchangeConfig> getExchangeConfigs() { return exchangeConfigs.values(); }
    public ExchangeHandler getHandlerByAcronym(String acronym) { return activeHandlers.get(acronym); }

    //TO simulate live updates to the status page until I can make a good live simulation...
    public void simulateLiveUpdates(int lowerLatency, int higherLatency, int delay, int period) {
        scheduler.scheduleAtFixedRate(() -> {
            exchangeConfigs.values().forEach(cfg -> {
                cfg.setConnectionState(NmsExchangeConfig.ConnectionState.CONNECTED);
                cfg.setLastLatencyMs(ThreadLocalRandom.current().nextInt(lowerLatency, higherLatency));
                cfg.setLastMessageTimestamp(Instant.now());
            });
        }, delay, period, TimeUnit.SECONDS);
    }

    public void scheduleEndOfDaySequenceNumberReset() {
        if (!Boolean.parseBoolean(configs.getProperty("gateway.resetSequenceOnEOD", "true"))) {
            return;
        }
        long delay = Duration.between(LocalTime.now(), endOfDayTime).toMillis();
        scheduler.schedule(() -> {
            activeHandlers.forEach((acronym, handler) -> {
                if (handler instanceof FixProtocolHandler fixHandler) {
                    FixSessionManager session = fixHandler.getSessionManager();
                    if (session != null) {
                        session.endSession();
                        session.resetSeqNum();
                        logger.info("End-of-day reset for " + acronym);
                    } else {
                        logger.warning("No session manager found for " + acronym);
                    }
                }
            });
            // Reschedule for next day
            scheduleEndOfDaySequenceNumberReset();
        }, delay, TimeUnit.MILLISECONDS);
        LocalTime nextReset = LocalTime.now().plus(Duration.ofMillis(delay)).truncatedTo(ChronoUnit.SECONDS);
        logger.info("Next FIX sequence reset scheduled for: " + nextReset);
    }

    private void scheduleNextReset(Runnable task) {
        long delay = Duration.between(LocalTime.now(), endOfDayTime).toMillis();
        if (delay < 0) delay += Duration.ofDays(1).toMillis(); // wrap to next day

        scheduler.schedule(task, delay, TimeUnit.MILLISECONDS);
    }

    public GatewayCore(ConcurrentHashMap<String, NmsExchangeConfig> exchangeConfigs, Properties configs) {
        this.exchangeConfigs = exchangeConfigs;
        this.configs = configs;
        handlerFactories.put("fix", new FixHandlerFactory());
        handlerFactories.put("json", new JsonHandlerFactory());
        String eod = configs.getProperty("gateway.endOfDayTime", "16:30");
        endOfDayTime = LocalTime.parse(eod); // assumes HH:mm format
        int threadCount = Integer.parseInt(configs.getProperty("gateway.threadPoolSize", "8"));
        String model = configs.getProperty("gateway.threadingModel", "fixed");
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

//        simulateLiveUpdates(5, 150, 10, 5); //TODO - remove
        scheduleEndOfDaySequenceNumberReset();
    }

    public void start() {
        logger.info("GatewayCore starting with " + exchangeConfigs.size() + " configured exchanges.");
        exchangeConfigs.values().forEach(cfg -> {
            cfg.setConnectionState(NmsExchangeConfig.ConnectionState.SESSION_CLOSED);
        });
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
                    activeHandlers.put(exchangeConfig.getAcronym(), handler);
                    handler.handle(socket);
                }
                else {
                    logger.warning("Unknown handler: " + exchangeConfig.getHandlerName());
                    socket.close();
                }
                exchangeConfig.setConnectionState(NmsExchangeConfig.ConnectionState.CONNECTED);
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
            exchangeConfig.setConnectionState(NmsExchangeConfig.ConnectionState.FAILED);
            logger.severe("[" + exchangeConfig.getAcronym() + "] All connection attempts failed.");
            // TODO: Apply recovery behavior
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

    public List<ExchangeStatus> getExchangeStatuses() {
        return exchangeConfigs.values().stream()
                .map(cfg -> new ExchangeStatus(
                        cfg.getAcronym(),
                        cfg.getConnectionStatus(),
                        cfg.getLastLatencyMs(),          // updated by your handler logic
                        cfg.getLastMessageTimestamp()    // updated on each message
                ))
                .collect(Collectors.toList());
    }

    public NmsExchangeConfig getExchangeConfigByAcronym(String acronym) {
        if (acronym == null || acronym.isBlank()) return null;
        NmsExchangeConfig cfg = exchangeConfigs.get(acronym);
        if (cfg == null) {
            logger.warning("Exchange acronym not found: " + acronym);
        }
        return cfg;
    }

    public void disconnect(NmsExchangeConfig exchangeConfig) {
        exchangeConfig.setConnectionState(NmsExchangeConfig.ConnectionState.SESSION_CLOSED);

        // Cancel the active connection task
        Future<?> task = activeConnections.remove(exchangeConfig.getAcronym());
        if (task != null) {
            task.cancel(true);
            logger.info("Cancelled active task for " + exchangeConfig.getAcronym());
        }

        // End FIX session and close resources
        ExchangeHandler handler = activeHandlers.remove(exchangeConfig.getAcronym());
        if (handler instanceof FixProtocolHandler fixHandler) {
            fixHandler.disconnect();
            logger.info("Disconnected FIX handler for " + exchangeConfig.getAcronym());
        } else {
            logger.warning("No FIX handler found for " + exchangeConfig.getAcronym());
        }
    }

    public void connect(NmsExchangeConfig exchangeConfig) {
        exchangeConfig.setConnectionState(NmsExchangeConfig.ConnectionState.CONNECTING);
        Future<?> task = executor.submit(() -> connectToExchange(exchangeConfig, configs));
        activeConnections.put(exchangeConfig.getAcronym(), task);
        logger.info("Connect triggered for " + exchangeConfig.getAcronym());
    }

    public void sendLogon(String acronym) {
        NmsExchangeConfig cfg = lookupConfig(acronym);
        FixSessionManager fsm = lookupSessionManager(acronym);
        String msg = FixMessageBuilder.buildLogon(cfg, fsm);
        sendToExchange(cfg, msg);
    }

    public void sendLogout(String acronym) {
        NmsExchangeConfig cfg = lookupConfig(acronym);
        FixSessionManager fsm = lookupSessionManager(acronym);
        String msg = FixMessageBuilder.buildLogout(cfg, fsm);
        sendToExchange(cfg, msg);
    }

    public void sendHeartbeat(String acronym) {
        NmsExchangeConfig cfg = lookupConfig(acronym);
        FixSessionManager fsm = lookupSessionManager(acronym);
        String msg = FixMessageBuilder.buildHeartbeat(cfg, fsm);
        sendToExchange(cfg, msg);
    }

    public void sendTestRequest(String acronym, String testReqID) {
        NmsExchangeConfig cfg = lookupConfig(acronym);
        FixSessionManager fsm = lookupSessionManager(acronym);
        String msg = FixMessageBuilder.buildTestRequest(cfg, testReqID, fsm);
        sendToExchange(cfg, msg);
    }

    /**
     * Look up the FixSessionManager for a given exchange acronym.
     */
    private FixSessionManager lookupSessionManager(String acronym) {
        ExchangeHandler handler = activeHandlers.get(acronym);
        if (handler instanceof FixProtocolHandler fixHandler) {
            return fixHandler.getSessionManager();
        }
        logger.warning("No FixSessionManager found for acronym: " + acronym);
        throw new IllegalStateException("No session manager for " + acronym);
    }


    /**
     * Look up the exchange configuration by acronym.
     */
    private NmsExchangeConfig lookupConfig(String acronym) {
        if (acronym == null || acronym.isBlank()) {
            throw new IllegalArgumentException("Acronym cannot be null or blank");
        }
        NmsExchangeConfig cfg = exchangeConfigs.get(acronym);
        if (cfg == null) {
            logger.warning("Exchange config not found for acronym: " + acronym);
            throw new IllegalStateException("No config for " + acronym);
        }
        return cfg;
    }

    /**
     * Send a raw FIX message string to the exchange.
     */
    private void sendToExchange(NmsExchangeConfig cfg, String msg) {
        ExchangeHandler handler = activeHandlers.get(cfg.getAcronym());
        if (handler instanceof FixProtocolHandler fixHandler) {
            fixHandler.sendRaw(msg);
            logger.info("[{" + cfg.getAcronym() + "}][OUT] {" + msg.replace('\u0001', '|') + "}");
        } else {
            logger.warning("No active FIX handler for " + cfg.getAcronym());
        }
    }

    public void shutdown() {
        logger.info("Shutting down GatewayCore...");
        //End all sessions
        activeHandlers.forEach((acronym, handler) -> {
            if (handler instanceof FixProtocolHandler fixHandler) {
                fixHandler.getSessionManager().endSession();
                logger.info("Ended FIX session for " + acronym);
            }
        });
        //close all log files
        activeHandlers.values().forEach(ExchangeHandler::close);
        //End all connections
        activeConnections.forEach((acronym, task) -> {
            NmsExchangeConfig cfg = getExchangeConfigByAcronym(acronym);
            if (cfg != null) {
                disconnect(cfg);
            } else {
                logger.warning("No config found for acronym: " + acronym);
                task.cancel(true);
            }
        });
        //shutdown thread pool
        executor.shutdown();
        try {
            if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
        //shutdown thread pool
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }

        logger.info("GatewayCore shutdown complete.");
    }


}