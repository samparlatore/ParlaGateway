package com.parlAquatics.gateway.protocols.fix;

import com.parlAquatics.gateway.jetty.cfg.NmsExchangeConfig;
import quickfix.SessionID;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class SessionRegistry {
    private final Map<String, SessionID> acronymToSessionId = new ConcurrentHashMap<>();
    private final Map<String, NmsExchangeConfig> acronymToNmsExchangeConfig = new ConcurrentHashMap<>();

    public void register(String acronym, SessionID sessionId, NmsExchangeConfig cfg) {
        acronymToSessionId.put(acronym, sessionId);
        acronymToNmsExchangeConfig.put(acronym, cfg);
    }

    public SessionID getSessionId(String acronym) {
        return acronymToSessionId.get(acronym);
    }

    public NmsExchangeConfig getNmsExchangeConfig(String acronym) {
        return acronymToNmsExchangeConfig.get(acronym);
    }

    public boolean hasSession(String acronym) {
        return acronymToSessionId.containsKey(acronym);
    }

    public Set<String> getRegisteredAcronyms() {
        return acronymToSessionId.keySet();
    }

    public String getAcronymFor(SessionID sessionId) {
        for (Map.Entry<String, SessionID> entry : acronymToSessionId.entrySet()) {
            if (entry.getValue().equals(sessionId)) {
                return entry.getKey();
            }
        }
        throw new IllegalStateException("No acronym found for session: " + sessionId);
    }
}
