package com.parlAquatics.gateway.test.testServer;

import com.parlAquatics.gateway.jetty.cfg.NmsExchangeConfig;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NmsTestServer {
    private static final int START_PORT = 25001;
    private static final int END_PORT = 25020;

    public static void main(String[] args) throws IOException {
        List<NmsExchangeConfig> exchanges = loadExchangeConfigs("nmsExchange.properties");
        ExecutorService pool = Executors.newFixedThreadPool(exchanges.size());

        for (NmsExchangeConfig config : exchanges) {
            pool.submit(() -> startExchangeServer(config));
        }

        System.out.println("NMS Test Server running for " + exchanges.size() + " exchanges...");
    }

    private static void startExchangeServer(NmsExchangeConfig config) {
        try (ServerSocket serverSocket = new ServerSocket(config.getPort())) {
            while (true) {
                try (Socket client = serverSocket.accept();
                     OutputStream out = client.getOutputStream()) {
                    System.out.println("[" + config.getAcronym() + "] Connection on port " + config.getPort());
                    out.write(("OK from " + config.getAcronym() + "\n").getBytes());
                    out.flush();
                } catch (IOException e) {
                    System.err.println("[" + config.getAcronym() + "] Error: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("[" + config.getAcronym() + "] Failed to bind port " + config.getPort() + ": " + e.getMessage());
        }
    }

    public static List<NmsExchangeConfig> loadExchangeConfigs(String path) throws IOException {
        Properties nmsEchangeProps = new Properties();
        try (FileInputStream fis = new FileInputStream(path)) {
            nmsEchangeProps.load(fis);
        }

        List<NmsExchangeConfig> exchanges = new ArrayList<>();
        for (int i = 1; ; i++) {
            String prefix = "nmsExchange." + i + ".";
            String name = nmsEchangeProps.getProperty(prefix + "name");
            if (name == null || name.isBlank()) break;

            NmsExchangeConfig config = new NmsExchangeConfig(
                    name,
                    nmsEchangeProps.getProperty(prefix + "acronym"),
                    nmsEchangeProps.getProperty(prefix + "location"),
                    nmsEchangeProps.getProperty(prefix + "ipAddress"),
                    Integer.parseInt(nmsEchangeProps.getProperty(prefix + "port")),
                    Integer.parseInt(nmsEchangeProps.getProperty(prefix + "latencyAlert")),
                    nmsEchangeProps.getProperty(prefix + "handlerName", "default"),
                    Integer.parseInt(nmsEchangeProps.getProperty(prefix + "connectionRetries", "3")),
                    Integer.parseInt(nmsEchangeProps.getProperty(prefix + "connectionRetryInterval", "3000")),
                    nmsEchangeProps.getProperty(prefix + "connectionRecoveryBehavior", "default"),
                    nmsEchangeProps.getProperty(prefix + "connectionStartTime", "0930"),
                    nmsEchangeProps.getProperty(prefix + "connectionEndTime", "1600"),

                    nmsEchangeProps.getProperty(prefix + "connectionType", "initiator"),
                    nmsEchangeProps.getProperty(prefix + "senderCompID", "GATEWAY"),
                    nmsEchangeProps.getProperty(prefix + "targetCompID", "EXCHANGE"),
                    Integer.parseInt(nmsEchangeProps.getProperty(prefix + "heartBtInt", "30")),
                    nmsEchangeProps.getProperty(prefix + "useDataDictionary", "Y"),
                    nmsEchangeProps.getProperty(prefix + "dataDictionary", "FIX42.xml"),
                    nmsEchangeProps.getProperty(prefix + "fileStorePath", "store"),
                    nmsEchangeProps.getProperty(prefix + "fileLogPath", "log")

            );

            exchanges.add(config);
        }
        return exchanges;
    }


}