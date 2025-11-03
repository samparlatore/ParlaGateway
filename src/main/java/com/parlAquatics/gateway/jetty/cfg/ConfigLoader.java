package com.parlAquatics.gateway.jetty.cfg;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * Created by Sam Parlatore for ParlAquatics Gateway.
 */
@WebListener
public class ConfigLoader implements ServletContextListener {
    private static final Logger logger = Logger.getLogger(ConfigLoader.class.getName());
    private ConcurrentHashMap<String,NmsExchangeConfig> exchangeConfigs = new ConcurrentHashMap<>();

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        try {
            // Load custom logging configuration
            String loggingPath = "src/main/resources/logging.properties"; // adjust if needed
            File loggingFile = new File(loggingPath);
            if (loggingFile.exists()) {
                LogManager.getLogManager().readConfiguration(new FileInputStream(loggingFile));
                logger.info("Loaded logging config from " + loggingFile.getAbsolutePath());
            } else {
                logger.warning("Logging config file not found at " + loggingFile.getAbsolutePath());
            }

            // Load tokens from config.properties
            Properties props = new Properties();
            props.load(new FileInputStream("config.properties"));

            for (String key : props.stringPropertyNames()) {
                String value = props.getProperty(key);
                sce.getServletContext().setAttribute(key, value);
                logger.info("Loaded config: " + key + " = " + value);
            }
            //Add the general configurations to the server context.
            sce.getServletContext().setAttribute("generalConfigs", props);

            // Load tokens from nmsExchange.properties
            Properties nmsExchangeProps = new Properties();
            nmsExchangeProps.load(new FileInputStream("nmsExchange.properties"));

            exchangeConfigs.clear();
            for (int i = 1; ; i++) {
                String prefix = "nmsExchange." + i + ".";
                String name = nmsExchangeProps.getProperty(prefix + "name");
                //logger.info("Loaded config: " + prefix + "name" + " = " + nmsEchangeProps.getProperty(prefix + "name"));
                if (name == null || name.isBlank()) {
                    //logger.info("No config found for index " + (i-1) + ". Stopping.");
                    break;
                }
                try {
                    NmsExchangeConfig config = new NmsExchangeConfig(
                            name,
                            nmsExchangeProps.getProperty(prefix + "acronym"),
                            nmsExchangeProps.getProperty(prefix + "location"),
                            nmsExchangeProps.getProperty(prefix + "ipAddress"),
                            Integer.parseInt(nmsExchangeProps.getProperty(prefix + "port")),
                            Integer.parseInt(nmsExchangeProps.getProperty(prefix + "latencyAlert", "1000")),

                            nmsExchangeProps.getProperty(prefix + "beginString", "FIX.4.4"),
                            nmsExchangeProps.getProperty(prefix + "senderCompID", "GATEWAY"),
                            nmsExchangeProps.getProperty(prefix + "targetCompID", "EXCHANGE"),
                            Integer.parseInt(nmsExchangeProps.getProperty(prefix + "heartBtInt", "30")),

                            "Y".equalsIgnoreCase(nmsExchangeProps.getProperty(prefix + "useDataDictionary", "Y")),
                            nmsExchangeProps.getProperty(prefix + "dataDictionary", "FIX42.xml"),
                            nmsExchangeProps.getProperty(prefix + "fileStorePath", "store"),
                            nmsExchangeProps.getProperty(prefix + "fileLogPath", "log")
                    );

                    exchangeConfigs.put(config.getAcronym(), config);
                    logger.info("Loaded config for exchange: " + config.getAcronym());
                } catch (Exception e) {
                    logger.info("Error loading config for index " + i + ": " + e.getMessage());
                    break;
                }
            }
            //Add the exchangeConfigurations to the server context.
            sce.getServletContext().setAttribute("nmsExchangeConfigs", exchangeConfigs);

        } catch (Exception e) {
            logger.info("Error loading config for NMS exchanges: " + e.getMessage());
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        // Optional cleanup
    }
}
