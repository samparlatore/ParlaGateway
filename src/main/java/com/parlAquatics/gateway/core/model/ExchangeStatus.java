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
    private final int latencyAlertMillis;
    private final double lastMessageAgeSeconds;
    private final int heartbeatMillis;

    public double getLastMessageAgeSeconds() {
        return lastMessageAgeSeconds;
    }
    public int getLatencyAlertMillis() {
        return latencyAlertMillis;
    }

    public ExchangeStatus(String acronym, String status, long latencyMicros, Instant lastMessageTimestamp, String lastMessageType, int latencyAlertMillis, int heartbeatMillis) {
        this.acronym = acronym;
        this.status = status;
        this.latencyMicros = latencyMicros;
        this.lastMessageTimestamp = lastMessageTimestamp;
        this.lastMessageType = lastMessageType;
        this.latencyAlertMillis = latencyAlertMillis;
        this.lastMessageAgeSeconds = lastMessageTimestamp == null ? 0 : Duration.between(lastMessageTimestamp, Instant.now()).toMillis() / 1000.0;
        this.heartbeatMillis = heartbeatMillis;
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

    public long getLastMessageAgeMillis() {
        return lastMessageTimestamp == null ? -1 : Duration.between(lastMessageTimestamp, Instant.now()).toMillis();
    }

    public String getLastMessageType() {
        return lastMessageType != null ? lastMessageType : "—";
    }

    public int getHeartbeatMillis() {
        return heartbeatMillis;
    }

}