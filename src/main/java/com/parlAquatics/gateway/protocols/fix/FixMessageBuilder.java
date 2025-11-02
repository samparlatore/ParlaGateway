package com.parlAquatics.gateway.protocols.fix;

import com.parlAquatics.gateway.jetty.cfg.NmsExchangeConfig;
import quickfix.Message;
import quickfix.field.*;
import quickfix.fix44.Heartbeat;
import quickfix.fix44.Logon;
import quickfix.fix44.Logout;
import quickfix.fix44.TestRequest;

public class FixMessageBuilder {

    public static String buildHeartbeat(NmsExchangeConfig cfg, FixSessionManager fsm) {
        Heartbeat hb = new Heartbeat();
        setStandardHeader(hb, cfg, fsm);
        return hb.toString();
    }

    public static String buildLogon(NmsExchangeConfig cfg, FixSessionManager fsm) {
        Logon logon = new Logon(new EncryptMethod(0), new HeartBtInt(30));
        setStandardHeader(logon, cfg, fsm);
        return logon.toString();
    }

    public static String buildLogout(NmsExchangeConfig cfg, FixSessionManager fsm) {
        Logout logout = new Logout();
        setStandardHeader(logout, cfg, fsm);
        return logout.toString();
    }

    public static String buildTestRequest(NmsExchangeConfig cfg, String testReqID, FixSessionManager fsm) {
        TestRequest tr = new TestRequest(new TestReqID(testReqID));
        setStandardHeader(tr, cfg, fsm);
        return tr.toString();
    }

    private static void setStandardHeader(Message msg, NmsExchangeConfig cfg, FixSessionManager fsm) {
        msg.getHeader().setField(new BeginString(cfg.getBeginString()));   // e.g. "FIX.4.4"
        msg.getHeader().setField(new SenderCompID(cfg.getSenderCompID()));
        msg.getHeader().setField(new TargetCompID(cfg.getTargetCompID()));
        msg.getHeader().setField(new MsgSeqNum(fsm.nextSeqNum()));
        msg.getHeader().setField(new SendingTime());
    }
}


//    public static String buildHeartbeat(NmsExchangeConfig cfg, FixSessionManager fsm) {
//        StringBuilder body = new StringBuilder();
//        body.append("35=0").append('|');
//        body.append("49=").append(cfg.getSenderCompID()).append('|');
//        body.append("56=").append(cfg.getTargetCompID()).append('|');
//        body.append("34=").append(fsm.nextSeqNum()).append('|');
//        body.append("52=").append(Instant.now().toString()).append('|');
//
//        return wrap(body.toString(), cfg);
//    }
//
//    public static String buildLogon(NmsExchangeConfig cfg, FixSessionManager fsm) {
//        StringBuilder body = new StringBuilder();
//        body.append("35=A").append('|');
//        body.append("49=").append(cfg.getSenderCompID()).append('|');
//        body.append("56=").append(cfg.getTargetCompID()).append('|');
//        body.append("34=").append(fsm.nextSeqNum()).append('|');
//        body.append("52=").append(Instant.now().toString()).append('|');
//        body.append("98=0").append('|'); // encryption: none
//        body.append("108=30").append('|'); // heartbeat interval
//
//        return wrap(body.toString(), cfg);
//    }
//
//    public static String buildLogout(NmsExchangeConfig cfg, FixSessionManager fsm) {
//        StringBuilder body = new StringBuilder();
//        body.append("35=5").append('|');
//        body.append("49=").append(cfg.getSenderCompID()).append('|');
//        body.append("56=").append(cfg.getTargetCompID()).append('|');
//        body.append("34=").append(fsm.nextSeqNum()).append('|');
//        body.append("52=").append(Instant.now().toString()).append('|');
//
//        return wrap(body.toString(), cfg);
//    }
//
//    public static String buildTestRequest(NmsExchangeConfig cfg, String testReqID, FixSessionManager fsm) {
//        StringBuilder body = new StringBuilder();
//        body.append("35=1").append('|');
//        body.append("49=").append(cfg.getSenderCompID()).append('|');
//        body.append("56=").append(cfg.getTargetCompID()).append('|');
//        body.append("34=").append(fsm.nextSeqNum()).append('|');
//        body.append("52=").append(Instant.now().toString()).append('|');
//        body.append("112=").append(testReqID).append('|'); // TestReqID
//
//        return wrap(body.toString(), cfg);
//    }
//
//    private static String wrap(String bodyFields, NmsExchangeConfig cfg) {
//        String body = bodyFields.replace('|', '\u0001');
//        int bodyLength = body.getBytes(StandardCharsets.US_ASCII).length;
//
//        StringBuilder full = new StringBuilder();
//        full.append("8=").append(cfg.getBeginString()).append('\u0001');
//        full.append("9=").append(bodyLength).append('\u0001');
//        full.append(body);
//        full.append("10=").append(calculateChecksum(full.toString())).append('\u0001');
//
//        return full.toString();
//    }
//
//    private static String calculateChecksum(String msg) {
//        byte[] bytes = msg.getBytes(StandardCharsets.US_ASCII);
//        int sum = 0;
//        for (byte b : bytes) {
//            sum += b;
//        }
//        int checksum = sum % 256;
//        return String.format("%03d", checksum);
//    }
//}