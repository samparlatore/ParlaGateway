package com.parlAquatics.gateway.simulation.testServer;

import com.parlAquatics.gateway.jetty.cfg.NmsExchangeConfig;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
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
                try {
                    Socket client = serverSocket.accept();
                    OutputStream out = client.getOutputStream();
                    BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                    System.out.println("[" + config.getAcronym() + "] Connection on port " + config.getPort());

                    boolean logonReceived = false;
                    InputStream inputS = client.getInputStream();
                    byte[] buffer = new byte[1024];
                    int len;
                    while ((len = inputS.read(buffer)) != -1) {
                        String inbound = new String(buffer, 0, len, StandardCharsets.US_ASCII);
                        System.out.println("[" + config.getAcronym() + "] Received: " + inbound);


                        if (!logonReceived && inbound.contains("35=A")) {
                            logonReceived = true;

                            String logonResponse = "8=FIX.4.2\u00019=65\u000135=A\u000134=1\u000149=" + config.getTargetCompID() +
                                    "\u000156=" + config.getSenderCompID() + "\u000152=20251102-14:30:00\u000198=0\u0001108=" +
                                    config.getHeartBtInt() + "\u000110=000\u0001";

                            out.write(logonResponse.getBytes());
                            out.flush();
                            System.out.println("[" + config.getAcronym() + "] Sent Logon response " + logonResponse);
                            continue;
                        }

                        if (logonReceived && inbound.contains("35=1")) { // TestRequest
                            String hb = "8=FIX.4.2\u00019=12\u000135=0\u000134=2\u000149=" + config.getTargetCompID() +
                                    "\u000156=" + config.getSenderCompID() + "\u000152=20251102-14:30:10\u000110=000\u0001";
                            out.write(hb.getBytes());
                            out.flush();
                            System.out.println("[" + config.getAcronym() + "] Responded to TestRequest with Heartbeat");
                        }

                        // You can add more message types here later (e.g., Logout, ExecutionReport, etc.)
                    }
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
                    nmsEchangeProps.getProperty(prefix + "fileLogPath", "log"),
                    nmsEchangeProps.getProperty(prefix + "beginString", "FIX.4.4")
            );

            exchanges.add(config);
        }
        return exchanges;
    }


}