package com.parlAquatics.gateway.protocols.fix;

import com.parlAquatics.gateway.jetty.cfg.NmsExchangeConfig;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

public class FixSessionManager {
    private static final Logger logger = Logger.getLogger(FixSessionManager.class.getName());
    private final NmsExchangeConfig cfg;
    private boolean active;
    private boolean resetRequested;
    private Instant lastInbound;
    private Instant lastOutbound;
    private final AtomicInteger outboundSeqNum = new AtomicInteger(1);
    private final AtomicInteger inboundSeqNum = new AtomicInteger(1);
    private final Path outboundSeqFile;
    private final Path inboundSeqFile;

    public FixSessionManager(NmsExchangeConfig cfg) {
        this.cfg = cfg;
        this.outboundSeqFile = Paths.get(cfg.getFileStorePath() + File.separator + "seq" + File.separator + cfg.getAcronym(), cfg.getAcronym() + ".outbound.seq");
        this.inboundSeqFile = Paths.get(cfg.getFileStorePath() + File.separator + "seq" + File.separator + cfg.getAcronym(), cfg.getAcronym() + ".inbound.seq");
        loadSeqNums();
    }

    public void startSession(boolean resetSeqNum) {
        active = true;
        if (resetSeqNum) resetSeqNum();
    }

    private void persistOutboundSeq(int value) {
        try {
            Files.createDirectories(outboundSeqFile.getParent());
            Files.writeString(outboundSeqFile, String.valueOf(value), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            logger.warning("Failed to persist outbound FIX seq for " + cfg.getAcronym());
        }
    }

    private void persistInboundSeq(int value) {
        try {
            Files.createDirectories(inboundSeqFile.getParent());
            Files.writeString(inboundSeqFile, String.valueOf(value), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            logger.warning("Failed to persist inbound FIX seq for " + cfg.getAcronym());
        }
    }

    private void loadSeqNums() {
        try {
            if (Files.exists(outboundSeqFile)) {
                int value = Integer.parseInt(Files.readString(outboundSeqFile).trim());
                outboundSeqNum.set(value);
                logger.info("Loaded outbound FIX seq for " + cfg.getAcronym() + ": " + value);
            }
        } catch (Exception e) {
            outboundSeqNum.set(1);
            logger.warning("Failed to load outbound FIX seq for " + cfg.getAcronym());
        }

        try {
            if (Files.exists(inboundSeqFile)) {
                int value = Integer.parseInt(Files.readString(inboundSeqFile).trim());
                inboundSeqNum.set(value);
                logger.info("Loaded inbound FIX seq for " + cfg.getAcronym() + ": " + value);
            }
        } catch (Exception e) {
            inboundSeqNum.set(1);
            logger.warning("Failed to load inbound FIX seq for " + cfg.getAcronym());
        }
    }

    public void endSession() {
        active = false;
    }

    public boolean isActive() {
        return active;
    }

    public int nextSeqNum() {
        int next = outboundSeqNum.getAndIncrement();
        persistOutboundSeq(next + 1);
        return next;
    }

    public void markInbound(int receivedSeqNum) {
        lastInbound = Instant.now();
        inboundSeqNum.set(receivedSeqNum);
        persistInboundSeq(receivedSeqNum);
    }


    public void resetSeqNum() {
        outboundSeqNum.set(1);
        inboundSeqNum.set(1);
        persistOutboundSeq(1);
        persistInboundSeq(1);
    }

    public void markInbound() {
        lastInbound = Instant.now();
    }

    public void markOutbound() {
        lastOutbound = Instant.now();
        cfg.setLastMessageTimestamp(lastOutbound);
    }

    public Duration timeSinceLastInbound() {
        return Duration.between(lastInbound, Instant.now());
    }

    public Duration timeSinceLastOutbound() {
        return Duration.between(lastOutbound, Instant.now());
    }
}