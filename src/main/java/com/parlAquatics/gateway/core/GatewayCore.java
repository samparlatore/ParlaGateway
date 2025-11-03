package com.parlAquatics.gateway.core;

import com.parlAquatics.gateway.core.model.ExchangeStatus;
import com.parlAquatics.gateway.jetty.cfg.NmsExchangeConfig;

import com.parlAquatics.gateway.protocols.fix.FixMessageSender;
import com.parlAquatics.gateway.protocols.fix.QuickFIXApp;
import com.parlAquatics.gateway.protocols.fix.SessionRegistry;
import quickfix.*;

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
    private final Properties configs;
    private final Map<String, NmsExchangeConfig> exchangeConfigs;
    private final SessionRegistry sessionRegistry;
    private final Map<String, Initiator> initiators = new ConcurrentHashMap<>();
    private final FixMessageSender fixMessageSender;

    public Iterable<NmsExchangeConfig> getExchangeConfigs() { return exchangeConfigs.values(); }
    public SessionRegistry getSessionRegistry() { return sessionRegistry; }

    public FixMessageSender getFixMessageSender() { return fixMessageSender; }

    public GatewayCore(ConcurrentHashMap<String, NmsExchangeConfig> exchangeConfigs, Properties configs) {
        this.exchangeConfigs = exchangeConfigs;
        this.configs = configs;
        this.sessionRegistry = new SessionRegistry();
        this.fixMessageSender = new FixMessageSender(sessionRegistry);
        //setup the QuickFix components for each exchange.
        for (NmsExchangeConfig cfg : exchangeConfigs.values()) {
            SessionID sessionId = cfg.buildQuickFIXSessionID();
            Dictionary dic = cfg.buildQuickFIXDictionary();
            SessionSettings settings = new SessionSettings();
            try {
                settings.set(sessionId, dic);
                Application app = new QuickFIXApp(sessionRegistry);
                MessageStoreFactory storeFactory = new FileStoreFactory(settings);
                LogFactory logFactory = new FileLogFactory(settings);
                MessageFactory msgFactory = new DefaultMessageFactory();

                Initiator initiator = new SocketInitiator(app, storeFactory, settings, logFactory, msgFactory);
                initiators.put(cfg.getAcronym(), initiator);
            } catch (ConfigError e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void start() {
        logger.info("GatewayCore starting with " + exchangeConfigs.size() + " configured exchanges.");
        exchangeConfigs.values().forEach(cfg -> {
            cfg.setConnectionState(NmsExchangeConfig.ConnectionState.SESSION_CLOSED);
            // Build SessionID from config and Register in SessionRegistry
            sessionRegistry.register(cfg.getAcronym(), cfg.buildQuickFIXSessionID(), cfg);
            logger.info("Registered session for " + cfg.getAcronym() + ": " + sessionRegistry.getSessionId(cfg.getAcronym()));
        });
    }

    public List<ExchangeStatus> getExchangeStatuses() {
        return exchangeConfigs.values().stream()
                .map(cfg -> new ExchangeStatus(
                        cfg.getAcronym(),
                        cfg.getConnectionStatus(),
                        cfg.getLastLatencyMicros(),
                        cfg.getLastMessageTimestamp(),
                        cfg.getLastMessageType(),
                        cfg.getLatencyAlert(),
                        cfg.getHeartBtInt() * 1000
                ))
                .collect(Collectors.toList());
    }

    public NmsExchangeConfig getExchangeConfigByAcronym(String acronym) {
        if (acronym == null || acronym.isBlank()) {
            logger.warning("Exchange acronym is null or blank");
            return null;
        }
        return exchangeConfigs.get(acronym);
    }

    public void connect(NmsExchangeConfig cfg) {
        logger.info("Manual connect triggered for " + cfg.getAcronym());
        try {
            if ( initiators.get(cfg.getAcronym()).isLoggedOn() ) { return; }
            initiators.get(cfg.getAcronym()).start();    // connect
            cfg.setConnectionState( NmsExchangeConfig.ConnectionState.CONNECTED );
        } catch (ConfigError e) {
            throw new RuntimeException(e);
        }
    }

    public void disconnect(String acronym) {
        logger.info("Manual disconnect triggered for " + acronym);
        try {
            if ( initiators.get(acronym).isLoggedOn()) {
                getFixMessageSender().sendLogoffMessage(acronym);
                initiators.get(acronym).stop();
            }
            getExchangeConfigByAcronym(acronym).setConnectionState( NmsExchangeConfig.ConnectionState.SESSION_CLOSED );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
