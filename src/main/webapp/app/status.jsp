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
        <h1>${pageTitle}</h1>
        <p>System is actively simulating exchange traffic and listening on port 9000.</p>
        <p class="timestamp">Generated at: <%= new java.util.Date() %></p>
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