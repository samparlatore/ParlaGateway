package com.parlAquatics.gateway.core.model;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class ExchangeStatus {
    private final String acronym;
    private final String status; // e.g., "connected", "disconnected", "reconnecting"
    private final long latencyMicros;
    private final Instant lastMessageTimestamp;
    private final String lastMessageType;

    public ExchangeStatus(String acronym, String status, long latencyMicros, Instant lastMessageTimestamp, String lastMessageType) {
        this.acronym = acronym;
        this.status = status;
        this.latencyMicros = latencyMicros;
        this.lastMessageTimestamp = lastMessageTimestamp;
        this.lastMessageType = lastMessageType;
    }

    public String getAcronym() {
        return acronym;
    }

    public String getStatus() {
        return status;
    }

    public long getLatencyMicros() {
        return latencyMicros;
    }

    public String getLatencyFormatted() {
        if (latencyMicros < 1000) return latencyMicros + "µs";
        return String.format("%.2f ms", latencyMicros / 1000.0);
    }

    public String getLastMessageTimestamp() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss.SSS").withZone(ZoneId.systemDefault());
        return lastMessageTimestamp != null ? formatter.format(lastMessageTimestamp) : "—";
    }

    public String getLastMessageAge() {
        if (lastMessageTimestamp == null) return "—";
        Duration age = Duration.between(lastMessageTimestamp, Instant.now());
        long millis = age.toMillis();
        if (millis < 1000) return millis + "ms ago";
        return String.format("%.1fs ago", millis / 1000.0);
    }

    public String getLastMessageType() {
        return lastMessageType != null ? lastMessageType : "—";
    }
}