package com.parlAquatics.gateway.jetty.cfg;

import quickfix.Dictionary;
import quickfix.SessionID;

import java.time.Instant;
import java.util.Map;
import java.util.Properties;

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

    // FIX session parameters
    private final String fixVersion;       // e.g. "FIX.4.4"
    private final String senderCompID;     // e.g. "GATEWAY"
    private final String targetCompID;     // e.g. "EXCHANGE"
    private final int heartBtInt;          // e.g. 30
    private final boolean useDataDictionary;
    private final String dataDictionary;   // optional
    private final String fileStorePath;
    private final String fileLogPath;

    // Runtime state
    private volatile ConnectionState connectionState = ConnectionState.SESSION_CLOSED;
    private volatile Instant lastMessageTimestamp;
    private volatile long lastLatencyMicros = 0;
    private volatile String lastMessageType = null;


    public enum ConnectionState {
        SESSION_CLOSED,
        CONNECTING,
        CONNECTED,
        FAILED
    }

    public NmsExchangeConfig(String name, String acronym, String location,
                             String ipAddress, int port, int latencyAlert,
                             String fixVersion, String senderCompID, String targetCompID,
                             int heartBtInt, boolean useDataDictionary, String dataDictionary,
                             String fileStorePath, String fileLogPath) {
        this.name = name;
        this.acronym = acronym;
        this.location = location;
        this.ipAddress = ipAddress;
        this.port = port;
        this.latencyAlert = latencyAlert;
        this.fixVersion = fixVersion;
        this.senderCompID = senderCompID;
        this.targetCompID = targetCompID;
        this.heartBtInt = heartBtInt;
        this.useDataDictionary = useDataDictionary;
        this.dataDictionary = dataDictionary;
        this.fileStorePath = fileStorePath;
        this.fileLogPath = fileLogPath;
    }

    // Getters
    public String getName() { return name; }
    public String getAcronym() { return acronym; }
    public String getLocation() { return location; }
    public String getIpAddress() { return ipAddress; }
    public int getPort() { return port; }
    public int getLatencyAlert() { return latencyAlert; }
    public String getFixVersion() { return fixVersion; }
    public String getSenderCompID() { return senderCompID; }
    public String getTargetCompID() { return targetCompID; }
    public int getHeartBtInt() { return heartBtInt; }
    public boolean isUseDataDictionary() { return useDataDictionary; }
    public String getDataDictionary() { return dataDictionary; }
    public String getFileStorePath() { return fileStorePath; }
    public String getFileLogPath() { return fileLogPath; }
    public long getLastLatencyMicros() { return lastLatencyMicros; }
    public String getLastMessageType() { return lastMessageType; }
    public Instant getLastMessageTimestamp() { return lastMessageTimestamp; }
    public void setLastMessageTimestamp(Instant timestamp) {  this.lastMessageTimestamp = timestamp; }
    public ConnectionState getConnectionState() { return connectionState; }
    public void setConnectionState(ConnectionState state) { this.connectionState = state; }
    public String getConnectionStatus() { return connectionState.name().toLowerCase().replace("_", " "); }

    public void recordMessage(String msgType, long latencyMicros) {
        this.lastMessageTimestamp = Instant.now();
        this.lastLatencyMicros = latencyMicros;
        this.lastMessageType = msgType;
    }


    // QuickFIX helpers
    public SessionID buildQuickFIXSessionID() {
        return new SessionID(fixVersion, senderCompID, targetCompID);
    }

    public Properties buildQuickFIXProperties() {
        Properties props = new Properties();

        props.setProperty("BeginString", fixVersion);
        props.setProperty("SenderCompID", senderCompID);
        props.setProperty("TargetCompID", targetCompID);
        props.setProperty("ConnectionType", "initiator");
        props.setProperty("SocketConnectHost", ipAddress);
        props.setProperty("SocketConnectPort", String.valueOf(port));
        props.setProperty("HeartBtInt", String.valueOf(heartBtInt));
        props.setProperty("StartTime", "00:00:00");
        props.setProperty("EndTime", "23:59:59");

        props.setProperty("ResetOnLogon", "Y");
        props.setProperty("ResetOnLogout", "Y");
        props.setProperty("ResetOnDisconnect", "Y");

        props.setProperty("UseDataDictionary", useDataDictionary ? "Y" : "N");
        if (useDataDictionary && dataDictionary != null && !dataDictionary.isBlank()) {
            props.setProperty("DataDictionary", dataDictionary);
        }

        props.setProperty("FileStorePath", fileStorePath + "/" + acronym);
        props.setProperty("FileLogPath", fileLogPath + "/" + acronym);
        //Don't start the sessions when initiator.start() is called.
        props.setProperty("AutoStart", "false"); //doesn't work for initiator
        return props;
    }

    public Dictionary buildQuickFIXDictionary() {
        Dictionary dic = new Dictionary();
        Properties props = buildQuickFIXProperties();
        for (Map.Entry<Object, Object> entry : props.entrySet()) {
            dic.setString(entry.getKey().toString(), entry.getValue().toString());
        }
        return dic;
    }


    @Override
    public String toString() {
        return String.format(
                "NmsExchangeConfig{name='%s', acronym='%s', location='%s', ip='%s', port=%d, latencyAlert=%d, " +
                        "fixVersion='%s', senderCompID='%s', targetCompID='%s', heartBtInt=%d, useDataDictionary=%s, " +
                        "dataDictionary='%s', fileStorePath='%s', fileLogPath='%s'}",
                name, acronym, location, ipAddress, port, latencyAlert,
                fixVersion, senderCompID, targetCompID, heartBtInt, useDataDictionary,
                dataDictionary, fileStorePath, fileLogPath
        );
    }
}
