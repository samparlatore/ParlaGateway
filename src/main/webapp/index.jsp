<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="java.util.logging.Logger" %>
<%
  Logger log = Logger.getLogger("JSPLogger");
  log.info("index rendered for " + request.getRemoteAddr());
%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <title>ParlaGateway</title>
    <link rel="stylesheet" type="text/css" href="/css/base.css">
</head>
<body>
    <div class="status-box">
        <h1>Welcome to ParlaGateway</h1>
        <p>This gateway simulates exchange traffic and reports system health, metrics, and runtime status.</p>

        <a href="/health" class="link-button">Health Check</a>
        <a href="/metrics" class="link-button">Metrics</a>
        <a href="/status" class="link-button">Status</a>
    </div>

    <div class="attribution">
        &copy; 2025 ParlAquatics LLC — Exchange Simulation Gateway
        <a href="http://www.freepik.com">Background Image by vecstock on Freepik</a>
    </div>
</body>
</html>
