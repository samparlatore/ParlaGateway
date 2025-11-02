package com.parlAquatics.gateway.protocols.fix;

import com.parlAquatics.gateway.jetty.app.MetricsServlet;
import com.parlAquatics.gateway.jetty.cfg.NmsExchangeConfig;
import com.parlAquatics.gateway.protocols.ExchangeHandler;

import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * Created by Sam Parlatore
 * Part of the ParlAquatics Gateway project
 *
 * This class handles protocol-specific socket processing for exchange connections.
 */
public class FixProtocolHandler implements ExchangeHandler {
    private static final Logger logger = Logger.getLogger(MetricsServlet.class.getName());
    ScheduledExecutorService heartbeatScheduler = Executors.newSingleThreadScheduledExecutor();
    private final NmsExchangeConfig config;
    private FixSessionManager session;
    private BufferedWriter logWriter;
    private Socket socket;
    private BufferedWriter writer;
    private Path logFilePath;


    public enum lfm {
        SENT,
        RECV
    }

    public FixSessionManager getSessionManager() { return session; }

    public FixProtocolHandler(NmsExchangeConfig cfg) {
        this.config = cfg;
        this.session = new FixSessionManager(cfg);
        try {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHH"));
            String logDir = cfg.getFileStorePath() + File.separator + cfg.getFileLogPath()  + File.separator + cfg.getAcronym(); // e.g., "log"
            String baseName = timestamp + "." + cfg.getAcronym() + ".fix.log";
            Path dir = Paths.get(logDir);
            Files.createDirectories(dir);
            int counter = 1;
            Path logFile;
            do {
                String numberedName = timestamp + "." + counter + "." + cfg.getAcronym() + ".fix.log";
                logFile = dir.resolve(numberedName);
                counter++;
            } while (Files.exists(logFile));
            logFile.toFile().getParentFile().mkdirs(); // ensure directory exists
            logWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(logFile.toFile(), true), StandardCharsets.US_ASCII));
            this.logFilePath = logFile;
            logger.info("Created log file for " + cfg.getAcronym() + ". " + logFile.toFile());
        } catch (IOException e) {
            logger.warning("Failed to initialize FIX log for " + cfg.getAcronym() + ": " + e.getMessage());
        }
    }

    public void startHeartbeat(NmsExchangeConfig exchangeConfig, OutputStream out) {
        heartbeatScheduler.scheduleAtFixedRate(() -> {
            String heartbeat = FixMessageBuilder.buildHeartbeat(exchangeConfig, session);
            try {
                out.write(heartbeat.getBytes(StandardCharsets.US_ASCII));
                out.flush();
                logger.info("Sent heartbeat to " + exchangeConfig.getAcronym());
            } catch (IOException e) {
                logger.warning("Failed to send heartbeat to " + exchangeConfig.getAcronym() + ": " + e.getMessage());
            }
        }, 0, 30, TimeUnit.SECONDS);
    }

    private void logFixMessage(lfm direction, String rawFix) {
        if (logWriter != null) {
            try {
                logWriter.write("[" + Instant.now() + "] " + direction + ": " + rawFix.replace('\u0001', '|'));
                logWriter.newLine();
                logWriter.flush();
            } catch (IOException e) {
                logger.warning("Failed to write FIX log for " + config.getAcronym() + ": " + e.getMessage());
            }
        }
    }

    public void handle(Socket socket) {
        this.socket = socket;
        BufferedReader reader = null;
        try {
            this.writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            session.startSession(true);

            Thread.sleep(250); //Let things simmer for a bit before sending the logon.
            String logon = FixMessageBuilder.buildLogon(config, session);
            writer.write(logon);
            writer.flush();
            session.markOutbound();

            logger.info("Sent FIX Logon to " + config.getAcronym());

            // Start heartbeat scheduler
            ScheduledExecutorService heartbeatScheduler = Executors.newSingleThreadScheduledExecutor();
            heartbeatScheduler.scheduleAtFixedRate(() -> {
                if (session.isActive()) {
                    String hb = FixMessageBuilder.buildHeartbeat(config, session);
                    try {
                        writer.write(hb);
                        writer.flush();
                        session.markOutbound();
                        logFixMessage(lfm.SENT, hb);
                        //logger.info("Sent Heartbeat to " + config.getAcronym());
                    } catch (IOException e) {
                        logger.warning("Failed to send heartbeat: " + e.getMessage());
                    }
                }
            }, config.getHeartBtInt(), config.getHeartBtInt(), TimeUnit.SECONDS);

            // Read inbound messages
            char[] buffer = new char[1024];
            int len;
            while ((len = reader.read(buffer)) != -1) {
                String inbound = new String(buffer, 0, len);
                Map<Integer, String> tags = FixMessageParser.parse(inbound);
                int receivedSeqNum = Integer.parseInt(tags.get(34));
                session.markInbound(receivedSeqNum);
                logFixMessage(lfm.RECV, inbound);

                String msgType = tags.get(35);
                switch (msgType) {
                    case "A":
                        logger.info("Received Logon from " + config.getAcronym());
                        break;
                    case "0":
                        logger.info("Received Heartbeat");
                        break;
                    case "1":
                        String testReqID = tags.get(112);
                        String hb = FixMessageBuilder.buildHeartbeat(config, session);
                        writer.write(hb);
                        writer.flush();
                        session.markOutbound();
                        logFixMessage(lfm.SENT, hb);
                        logger.info("Responded to TestRequest with Heartbeat");
                        break;
                    case "5":
                        logger.info("Received Logout from " + config.getAcronym());
                        session.endSession();
                        heartbeatScheduler.shutdownNow();
                        return;
                    default:
                        logger.info("Received FIX message type " + msgType);
                }
            }

        } catch (IOException e) {
            logger.warning("FIX handler error for " + config.getAcronym() + ": " + e.getMessage());
            session.endSession();
        } catch (InterruptedException e) {
            logger.warning("couldnt sleep before sending the logon to " + config.getAcronym() + ": " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() {
        disconnect();
        closeLogWriter();
    }

    @Override
    public void sendRaw(String msg) {
        if (msg == null || msg.isBlank()) {
            logger.warning("Attempted to send empty FIX message");
            return;
        }

        try {
            // Write the message out using the writer
            writer.write(msg);
            writer.flush();

            // Log a human‑readable version (SOH replaced with '|')
            logger.info("[" + config.getAcronym() + "][OUT] " + msg.replace('\u0001', '|'));

            // Optionally update session manager state
            if (session != null) {
                session.markOutbound();
            }

        } catch (IOException e) {
            logger.severe("Error sending FIX message for " + config.getAcronym() + ": " + e.getMessage());
        }
    }

    public void closeLogWriter() {
        if (logWriter != null) {
            try {
                logWriter.close();
                logger.info("Closed FIX log for " + config.getAcronym());
            } catch (IOException e) {
                logger.warning("Failed to close FIX log for " + config.getAcronym() + ": " + e.getMessage());
            }
        }
    }

    public void disconnect() {
        try {
            if (session.isActive() && writer != null) {
                String logout = FixMessageBuilder.buildLogout(config, session);
                writer.write(logout);
                writer.flush();
                session.markOutbound();
                logFixMessage(lfm.SENT, logout);
                logger.info("Sent FIX Logout to " + config.getAcronym());
            }
        } catch (IOException e) {
            logger.warning("Failed to send FIX Logout: " + e.getMessage());
        }

        try {
            if (writer != null) writer.close();
        } catch (IOException e) {
            logger.warning("Failed to close writer: " + e.getMessage());
        }

        session.endSession();

        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
                logger.info("Closed socket for " + config.getAcronym());
            }
        } catch (IOException e) {
            logger.warning("Failed to close socket: " + e.getMessage());
        }

        closeLogWriter();
    }

}

