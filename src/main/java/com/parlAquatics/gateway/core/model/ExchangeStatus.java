package com.parlAquatics.gateway.core.model;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class ExchangeStatus {
    private final String acronym;
    private final String status; // e.g., "connected", "disconnected", "reconnecting"
    private final int latencyMs;
    private final Instant lastMessageTimestamp;

    public ExchangeStatus(String acronym, String status, int latencyMs, Instant lastMessageTimestamp) {
        this.acronym = acronym;
        this.status = status;
        this.latencyMs = latencyMs;
        this.lastMessageTimestamp = lastMessageTimestamp;
    }

    public String getAcronym() {
        return acronym;
    }

    public String getStatus() {
        return status;
    }

    public int getLatencyMs() {
        return latencyMs;
    }

    public String getLastMessageTimestamp() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss.SSS").withZone(ZoneId.systemDefault());
        return lastMessageTimestamp != null ? formatter.format(lastMessageTimestamp) : "—";
    }

}

