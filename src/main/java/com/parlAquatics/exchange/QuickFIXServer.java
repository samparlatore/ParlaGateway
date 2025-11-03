package com.parlAquatics.exchange;

import quickfix.*;

import java.io.FileInputStream;

public class QuickFIXServer {
    private final SessionSettings settings;
    private final Application app;

    public QuickFIXServer() throws ConfigError {
        settings = new SessionSettings("quickFIXtestServer.cfg");
        app = new MyTestApplication();
        MessageStoreFactory storeFactory = new FileStoreFactory(settings);
        LogFactory logFactory = new FileLogFactory(settings);
        MessageFactory messageFactory = new DefaultMessageFactory();

        SocketAcceptor acceptor = new SocketAcceptor(app, storeFactory, settings, logFactory, messageFactory);
        acceptor.start();

    }


    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
//            System.out.println("Usage: MyTestExchangeApp <config_file_path>");
//            return;
            args = new String[] {"quickFIXtestServer.cfg"};
        }

        String fileName = args[0];
        Application application = new MyTestApplication();
        SessionSettings settings = new SessionSettings(new FileInputStream(fileName));

        MessageStoreFactory storeFactory = new FileStoreFactory(settings);
        LogFactory logFactory = new FileLogFactory(settings);
        MessageFactory messageFactory = new DefaultMessageFactory();

        Acceptor acceptor = new SocketAcceptor(application, storeFactory, settings, logFactory, messageFactory);

        acceptor.start();

        System.out.println("QuickFIX/J Exchange App (Acceptor) started. Listening for connections...");
        // Keep the application running.
        while (true) {
            Thread.sleep(1000);
        }

    }
}
