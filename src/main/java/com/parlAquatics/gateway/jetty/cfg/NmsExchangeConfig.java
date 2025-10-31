package com.parlAquatics.gateway.jetty.cfg;

/**
 * Created by Sam Parlatore
 * Part of the ParlAquatics Gateway project
 */
public class NmsExchangeConfig {
    private final String name;
    private final String acronym;
    private final String location;
    private final String ipAddress;
    private final int port;
    private final int latencyAlert;
    private final String handlerName;

    private final int connectionRetries;
    private final int connectionRetryInterval; // in ms
    private final String connectionRecoveryBehavior;
    private final String connectionStartTime; // e.g., "0930"
    private final String connectionEndTime;   // e.g., "1600"

    public NmsExchangeConfig(String name, String acronym, String location,
                             String ipAddress, int port, int latencyAlert,
                             String handlerName, int connectionRetries,
                             int connectionRetryInterval, String connectionRecoveryBehavior,
                             String connectionStartTime, String connectionEndTime) {
        this.name = name;
        this.acronym = acronym;
        this.location = location;
        this.ipAddress = ipAddress;
        this.port = port;
        this.latencyAlert = latencyAlert;
        this.handlerName = handlerName;
        this.connectionRetries = connectionRetries;
        this.connectionRetryInterval = connectionRetryInterval;
        this.connectionRecoveryBehavior = connectionRecoveryBehavior;
        this.connectionStartTime = connectionStartTime;
        this.connectionEndTime = connectionEndTime;
    }

    public String getName() { return name; }
    public String getAcronym() { return acronym; }
    public String getLocation() { return location; }
    public String getIpAddress() { return ipAddress; }
    public int getPort() { return port; }
    public int getLatencyAlert() { return latencyAlert; }
    public String getHandlerName() { return handlerName; }
    public int getConnectionRetries() { return connectionRetries; }
    public int getConnectionRetryInterval() { return connectionRetryInterval; }
    public String getConnectionRecoveryBehavior() { return connectionRecoveryBehavior; }
    public String getConnectionStartTime() { return connectionStartTime; }
    public String getConnectionEndTime() { return connectionEndTime; }

    // Optional: toString for debugging
    @Override
    public String toString() {
        return String.format("NmsExchangeConfig{name='%s', acronym='%s', location='%s', ip='%s', port=%d, latencyAlert=%d, handler='%s'}",
                name, acronym, location, ipAddress, port, latencyAlert, handlerName);
    }
}