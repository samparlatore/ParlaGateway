<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.logging.Logger" %>
<%
    Logger log = Logger.getLogger("JSPLogger");
    log.info("health.jsp rendered for " + request.getRemoteAddr());
%>
<!DOCTYPE html>
<html>
<head>
    <title><%= application.getAttribute("pageTitle") %></title>
    <link rel="stylesheet" type="text/css" href="/css/base.css">
</head>
<body>
    <div class="status-box">
        <h1><%= application.getAttribute("pageTitle") %></h1>
        <p>Status: <strong>OK</strong></p>
        <p class="timestamp">Uptime: <%= new java.util.Date().toString() %></p>
    </div>
    <script>
        document.addEventListener("click", function(event) {
            const box = document.querySelector(".status-box");
            if (box && !box.contains(event.target)) {
                window.location.href = "/";
            }
        });
    </script>
    <div class="attribution">
        &copy; 2025 ParlAquatics LLC — Exchange Simulation Gateway
    </div>
</body>
</html>