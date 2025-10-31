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

    public NmsExchangeConfig(String name, String acronym, String location,
                             String ipAddress, int port, int latencyAlert,
                             String handlerName, int connectionRetries,
                             int connectionRetryInterval, String connectionRecoveryBehavior,
                             String connectionStartTime, String connectionEndTime,
                             String connectionType, String senderCompID, String targetCompID,
                             int heartBtInt, String useDataDictionary, String dataDictionary,
                             String fileStorePath, String fileLogPath) {
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

    // Optional: toString for debugging
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
