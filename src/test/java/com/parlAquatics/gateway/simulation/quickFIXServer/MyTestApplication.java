package com.parlAquatics.gateway.simulation.quickFIXServer;

import quickfix.*;
import quickfix.field.*;
import quickfix.fix44.ExecutionReport;
import quickfix.field.LastQty;
import quickfix.Message;
import quickfix.FieldNotFound;
import quickfix.fix44.Logon;
import quickfix.fix44.OrderCancelReplaceRequest;
import quickfix.fix44.OrderCancelRequest;
import quickfix.fix44.OrderCancelReject;

import java.time.Instant;
import java.util.logging.Logger;

public class MyTestApplication extends MessageCracker implements Application {
    private static final Logger logger = Logger.getLogger(MyTestApplication.class.getName());


    // Callback methods for session and message events.
    // onCreate: Called when a new session is created.
    public void onCreate(SessionID sessionId) { logger.info("Session created: " + sessionId); }
    // onLogon: Called on successful logon.
    public void onLogon(SessionID sessionId) { logger.info("Logon successful for session: " + sessionId); }
    // onLogout: Called when a session is logged out.
    public void onLogout(SessionID sessionId) { logger.info("Session logged out: " + sessionId); }

    // toAdmin: Called before sending administrative messages.
    public void toAdmin(Message message, SessionID sessionId) { }
    // toApp: Called before sending application messages.
    public void toApp(Message message, SessionID sessionId) throws DoNotSend {
        logger.info("ToApp: " + message);
    }

    // fromAdmin: Called on receiving administrative messages.
    public void fromAdmin(Message message, SessionID sessionId) throws FieldNotFound, IncorrectDataFormat, IncorrectTagValue, RejectLogon {
        if (MsgType.LOGON.equals(message.getHeader().getString(MsgType.FIELD))) {
            Logon logon = (Logon) message;
            logger.info("Received Logon with HeartBtInt=" + logon.getHeartBtInt().getValue());
        }
    }

    // fromApp: Called on receiving application messages.
    public void fromApp(Message message, SessionID sessionId) throws FieldNotFound, IncorrectDataFormat, IncorrectTagValue, UnsupportedMessageType {
        logger.info("Received application message: " + message.toString());
        // Process the message. Using MessageCracker or annotations is recommended.
        crack(message, sessionId);
    }

    // Example handler for a NewOrderSingle (D)
    public void onMessage(quickfix.fix44.NewOrderSingle order, SessionID sessionId)
            throws FieldNotFound, IncorrectDataFormat, IncorrectTagValue, SessionNotFound {

        logger.info("Received NewOrderSingle: " + order.getClOrdID().getValue());

        // Simulate a full fill by sending an ExecutionReport.
        ExecutionReport report = new ExecutionReport();
        report.set(new OrderID("EXCHANGE_ORDER_ID_123"));
        report.set(order.getClOrdID());
        report.set(order.getSymbol());
        report.set(order.getSide());
        report.set(new OrdStatus(OrdStatus.FILLED));
        report.set(order.getOrderQty());
        report.set(new ExecType(ExecType.FILL));
        report.set(new LastQty(order.getOrderQty().getValue()));
        report.set(new CumQty(order.getOrderQty().getValue()));
        report.set(new AvgPx(order.getPrice().getValue()));
        report.set(new TransactTime());

        Session.sendToTarget(report, sessionId); // Send the report back to the initiator.
    }

    private void sendToTargetSafe(Message msg, SessionID sessionID) {
        try {
            Session.sendToTarget(msg, sessionID);
            logger.info("Sent: " + msg);
        } catch (SessionNotFound e) {
            logger.warning("Session not found: " + e.getMessage());
        }
    }

    // Handle OrderCancelRequest (F)
    public void onMessage(OrderCancelRequest cancel, SessionID sessionID)
            throws FieldNotFound, UnsupportedMessageType, IncorrectTagValue {
        logger.info("Received CancelRequest for ClOrdID=" + cancel.getClOrdID().getValue());

        // For testing, just reject all cancels
        OrderCancelReject reject = new OrderCancelReject();
        reject.set(new OrderID(cancel.getOrderID().getValue()));
        reject.set(new ClOrdID(cancel.getClOrdID().getValue()));
        reject.set(new CxlRejResponseTo(CxlRejResponseTo.ORDER_CANCEL_REQUEST));
        reject.set(new CxlRejReason(CxlRejReason.BROKER_EXCHANGE_OPTION));
        reject.set(new Text("Cancel not supported in test server"));
        if (cancel.isSetOrigClOrdID()) {
            reject.set(new OrigClOrdID(cancel.getOrigClOrdID().getValue()));
        }
        sendToTargetSafe(reject, sessionID);
    }

    // Handle OrderCancelReplaceRequest (G)
    public void onMessage(OrderCancelReplaceRequest replace, SessionID sessionID)
            throws FieldNotFound, UnsupportedMessageType, IncorrectTagValue {
        logger.info("Received CancelReplaceRequest for ClOrdID=" + replace.getClOrdID().getValue());

        // Simulate acceptance by sending a replaced ExecutionReport
        ExecutionReport report = new ExecutionReport();
        report.set(new OrderID("EXCHANGE_ORDER_ID_123"));
        report.set(replace.getClOrdID());
        report.set(new ExecType(ExecType.REPLACED));
        report.set(new OrdStatus(OrdStatus.REPLACED));
        report.set(replace.getSide());
        report.set(new LeavesQty(replace.getOrderQty().getValue()));
        report.set(new CumQty(0));
        report.set(new AvgPx(0));
        report.set(replace.getSymbol());
        report.set(replace.getOrderQty());
        report.set(new TransactTime());

        sendToTargetSafe(report, sessionID);
    }


}
