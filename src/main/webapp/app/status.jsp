<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="java.util.logging.Logger" %>
<%
    Logger log = Logger.getLogger("JSPLogger");
    log.info("status.jsp rendered for " + request.getRemoteAddr());
%>
<!DOCTYPE html>
<html>
<head>
    <title><%= application.getAttribute("pageTitle") %></title>
    <link rel="stylesheet" type="text/css" href="/css/base.css">
</head>
<body>
    <div class="status-box">
        <h1 class="dashboard-title">📊 ${pageTitle}</h1>
        <div class="dashboard-clock">
        <span title="Current Time">🕒 <strong id="currentTime">--:--:--.---</strong></span>
        <span title="Last Poll Update">🔄 <strong id="lastUpdated">--:--:--.---</strong></span>
        <span title="Stop updates"><button id="togglePolling">Pause</button></span>
        <span title="End all exchange connections"><button id="disconnectAll">Disconnect All</button></span>
        <span title="Connect to all configured exchanges"><button id="reconnectAll">Connect All</button></span>
        </div>
        <table id="exchangeStatus" class="status-table">
            <thead>
                <tr>
                    <th>Exchange</th>
                    <th>Status</th>
                    <th>Latency</th>
                    <th>Last Message</th>
                    <th>Age</th>
                    <th>Controls</th>
               </tr>
            </thead>
            <tbody>
                <tr><td colspan="6">Loading exchange status...</td></tr>
            </tbody>
        </table>
    </div>

    <script src="/app/status.js"></script>

    <div class="attribution">
        &copy; 2025 ParlAquatics LLC — Exchange Simulation Gateway
    </div>
</body>
</html>