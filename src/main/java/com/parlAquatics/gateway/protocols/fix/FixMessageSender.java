package com.parlAquatics.gateway.protocols.fix;

import quickfix.*;
import quickfix.field.TestReqID;
import quickfix.fix44.Heartbeat;
import quickfix.fix44.ResendRequest;
import quickfix.fix44.TestRequest;

import java.util.logging.Logger;

public class FixMessageSender {
    private static final Logger logger = Logger.getLogger(FixMessageSender.class.getName());
    private final SessionRegistry sessionRegistry;

    public FixMessageSender(SessionRegistry sessionRegistry) {
        this.sessionRegistry = sessionRegistry;
    }

    public void sendHeartbeat(String acronym) {
        logger.info("Manual sendHeartbeat triggered for " + acronym);
        sendIfLoggedOn(acronym, new Heartbeat());
    }

    public void sendTestRequest(String acronym) {
        logger.info("Manual sendTestRequest triggered for " + acronym);
        TestRequest testRequest = new TestRequest();
        testRequest.setField(new TestReqID("manual-ping"));
        sendIfLoggedOn(acronym, testRequest);
    }

    public void sendResendRequest(String acronym) {
        logger.info("Manual sendResendRequest triggered for " + acronym);
        ResendRequest resendRequest = new ResendRequest();
        resendRequest.set(new quickfix.field.BeginSeqNo(1)); // or session.getExpectedTargetNum()
        resendRequest.set(new quickfix.field.EndSeqNo(0));   // 0 = infinity
        sendIfLoggedOn(acronym, resendRequest);
    }

    public void sendLogoffMessage(String acronym) {
        logger.info("Manual sendLogoffMessage triggered for " + acronym);
        sendIfLoggedOn(acronym, new quickfix.fix44.Logout());
    }

    private void sendIfLoggedOn(String acronym, Message message) {
        SessionID sessionId = sessionRegistry.getSessionId(acronym);
        if (sessionId == null) {
            logger.warning("No sessionId found for " + acronym);
            return;
        }
        Session session = Session.lookupSession(sessionId);
        if (session == null || !session.isLoggedOn()) {
            logger.warning("Session not logged on for " + acronym);
            return;
        }
        session.send(message);
    }
}