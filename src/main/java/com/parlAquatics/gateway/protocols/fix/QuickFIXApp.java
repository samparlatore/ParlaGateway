package com.parlAquatics.gateway.protocols.fix;

import com.parlAquatics.gateway.jetty.cfg.NmsExchangeConfig;
import quickfix.*;
import quickfix.field.MsgType;

import java.util.logging.Logger;

/**
 * Created by Sam Parlatore
 * Handles all FIX sessions via shared Application instance
 */
public class QuickFIXApp implements Application {
    private static final Logger logger = Logger.getLogger(QuickFIXApp.class.getName());
    private final SessionRegistry sessionRegistry;

    public QuickFIXApp(SessionRegistry sessionRegistry) {
        this.sessionRegistry = sessionRegistry;
    }

    @Override
    public void onCreate(SessionID sessionId) {
        logger.info("Session created: " + sessionId);
    }

    @Override
    public void onLogon(SessionID sessionId) {
        String acronym = sessionRegistry.getAcronymFor(sessionId);
        NmsExchangeConfig cfg = sessionRegistry.getNmsExchangeConfig(acronym);
        if (cfg != null) {
            cfg.setConnectionState(NmsExchangeConfig.ConnectionState.CONNECTED);
            logger.info("Logon confirmed for " + acronym);
        }
    }

    @Override
    public void onLogout(SessionID sessionId) {
        String acronym = sessionRegistry.getAcronymFor(sessionId);
        NmsExchangeConfig cfg = sessionRegistry.getNmsExchangeConfig(acronym);
        if (cfg != null) {
            cfg.setConnectionState(NmsExchangeConfig.ConnectionState.SESSION_CLOSED);
            logger.info("Logout confirmed for " + acronym);
        }
    }

    @Override
    public void toAdmin(Message message, SessionID sessionId) {
        logger.fine("Sending admin message to " + sessionId + ": " + message);
    }

    @Override
    public void fromAdmin(Message message, SessionID sessionId) throws FieldNotFound, IncorrectDataFormat, IncorrectTagValue, RejectLogon {
        logger.fine("Received admin message from " + sessionId + ": " + message);
    }

    @Override
    public void toApp(Message message, SessionID sessionId) throws DoNotSend {
        logger.info("toApp: " + message.toString());
        try {
            long start = System.nanoTime();
            String msgType = message.getHeader().getString(MsgType.FIELD);
            long latencyMicros = (System.nanoTime() - start) / 1000;
            NmsExchangeConfig cfg = sessionRegistry.getNmsExchangeConfig(sessionRegistry.getAcronymFor(sessionId));
            if (cfg != null) {
                cfg.recordMessage(msgType, latencyMicros );
                logger.info("toApp: " + cfg.getAcronym() + " sent " + message.getHeader() + " (" + latencyMicros + "µs)");
            } else {
                logger.warning("toApp: No config found for session " + sessionId);
            }
        } catch (FieldNotFound e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void fromApp(Message message, SessionID sessionId) throws FieldNotFound, IncorrectDataFormat, IncorrectTagValue, UnsupportedMessageType {
        logger.info("fromApp: " + message.toString());
        long start = System.nanoTime();
        String msgType = message.getHeader().getString(MsgType.FIELD);
        long latencyMicros = (System.nanoTime() - start) / 1000;

        NmsExchangeConfig cfg = sessionRegistry.getNmsExchangeConfig(sessionRegistry.getAcronymFor(sessionId));
        if (cfg != null) {
            cfg.recordMessage(msgType, latencyMicros);
            logger.info("fromApp: " + cfg.getAcronym() + " received " + msgType + " (" + latencyMicros + "µs)");
        } else {
            logger.warning("fromApp: No config found for session " + sessionId);
        }
    }
}