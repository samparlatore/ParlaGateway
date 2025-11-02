package com.parlAquatics.gateway.jetty.cfg;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;

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
    private final int connectionRetryInterval;
    private final String connectionRecoveryBehavior;
    private final String connectionStartTime;
    private final String connectionEndTime;
    // FIX-specific configuration
    private final String connectionType;       // e.g., "initiator"
    private final String senderCompID;         // e.g., "GATEWAY"
    private final String targetCompID;         // e.g., "EXCHANGE"
    private final int heartBtInt;              // e.g., 30
    private final String useDataDictionary;    // e.g., "Y"
    private final String dataDictionary;       // e.g., "FIX42.xml"
    private final String fileStorePath;        // e.g., "store"
    private final String fileLogPath;          // e.g., "log"
    private final String beginString; // e.g. "FIX.4.4"

    // Runtime state (updated by handler logic)
    private volatile ConnectionState connectionState = ConnectionState.SESSION_CLOSED;
    private volatile int lastLatencyMs = -1;
    private volatile Instant lastMessageTimestamp = null;

    public enum ConnectionState {
        SESSION_CLOSED,     // Explicitly disconnected
        CONNECTING,         // Actively trying to connect
        CONNECTED,          // Fully connected
        FAILED              // All retries exhausted
    }

    public NmsExchangeConfig(String name, String acronym, String location,
                             String ipAddress, int port, int latencyAlert,
                             String handlerName, int connectionRetries,
                             int connectionRetryInterval, String connectionRecoveryBehavior,
                             String connectionStartTime, String connectionEndTime,
                             String connectionType, String senderCompID, String targetCompID,
                             int heartBtInt, String useDataDictionary, String dataDictionary,
                             String fileStorePath, String fileLogPath, String beginString) {
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

        this.connectionType = connectionType;
        this.senderCompID = senderCompID;
        this.targetCompID = targetCompID;
        this.heartBtInt = heartBtInt;
        this.useDataDictionary = useDataDictionary;
        this.dataDictionary = dataDictionary;
        this.fileStorePath = fileStorePath;
        this.fileLogPath = fileLogPath;
        this.beginString = beginString;
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
    public String getConnectionType() { return connectionType; }
    public String getSenderCompID() { return senderCompID; }
    public String getTargetCompID() { return targetCompID; }
    public int getHeartBtInt() { return heartBtInt; }
    public String getUseDataDictionary() { return useDataDictionary; }
    public String getDataDictionary() { return dataDictionary; }
    public String getFileStorePath() { return fileStorePath; }
    public String getFileLogPath() { return fileLogPath; }
    public int getLastLatencyMs() { return lastLatencyMs; }
    public Instant getLastMessageTimestamp() { return lastMessageTimestamp; }
    public void setLastLatencyMs(int latencyMs) {  this.lastLatencyMs = latencyMs;}
    public void setLastMessageTimestamp(Instant timestamp) { this.lastMessageTimestamp = timestamp;}
    public ConnectionState getConnectionState() { return connectionState; }
    public void setConnectionState(ConnectionState state) { this.connectionState = state; }
    public String getConnectionStatus() { return connectionState.name().toLowerCase().replace("_", " "); }
    public String getBeginString() { return beginString; }


    @Override
    public String toString() {
        return String.format(
                "NmsExchangeConfig{name='%s', acronym='%s', location='%s', ip='%s', port=%d, latencyAlert=%d, handler='%s', " +
                        "connectionType='%s', senderCompID='%s', targetCompID='%s', heartBtInt=%d, useDataDictionary='%s', " +
                        "dataDictionary='%s', fileStorePath='%s', fileLogPath='%s'}",
                name, acronym, location, ipAddress, port, latencyAlert, handlerName,
                connectionType, senderCompID, targetCompID, heartBtInt, useDataDictionary,
                dataDictionary, fileStorePath, fileLogPath
        );
    }

}
