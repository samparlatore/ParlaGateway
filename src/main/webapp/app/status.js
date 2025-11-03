let pollingActive = true;
let pollingInterval = null;


document.addEventListener("click", function(event) {
    const box = document.querySelector(".status-box");
    if (box && !box.contains(event.target)) {
        window.location.href = "/";
    }
});

function getLatencyClass(latencyMs) {
  if (latencyMs < 100) return "latency-ok";
  if (latencyMs < 300) return "latency-warn";
  return "latency-bad";
}


function getStatusClass(status) {
    const key = status.toLowerCase().replace(/\W+/g, '');
    return "status-" + key;
}

function updateExchangeTable(data) {
    const tbody = document.querySelector("#exchangeStatus tbody");
    tbody.innerHTML = "";
    data.forEach(exchange => {
        const row = document.createElement("tr");
        const latencyMs = exchange.latencyMicros / 1000;
        row.classList.add(getLatencyClass(latencyMs));
        const ageCellClass = (exchange.lastMessageAgeMillis > exchange.heartbeat)  ? "stale-age" : "";
        //console.log(`${exchange.acronym}: ageInMillis=${exchange.lastMessageAgeMillis}  heartbeat=${exchange.heartbeat}`);
        const ageDisplay = formatAge(exchange.lastMessageAgeMillis);
        //console.log(`${exchange.acronym}: latency=${exchange.latency}`);
        row.innerHTML = `
            <td><strong>${exchange.acronym}</strong></td>
            <td class="${getStatusClass(exchange.status)}">${exchange.status}</td>
            <td title="${exchange.latencyMicros} µs">${exchange.latencyFormatted}</td>
            <td title="${exchange.lastMessageType || '—'}">${exchange.lastMessage || '—'}</td>
            <td class="${ageCellClass}" title="Threshold: ${exchange.latencyAlertMillis} ms">${ageDisplay}</td>
            <td>
              <div class="control-buttons">
                <span title="connect"><button onclick="sendCommand('${exchange.acronym}', 'connect')">🔌</button></span>
                <span title="disconnect"><button onclick="sendCommand('${exchange.acronym}', 'disconnect')">❌</button></span>
                <span title="Send Heartbeat"><button onclick="sendCommand('${exchange.acronym}', 'heartbeat')">❤️</button></span>
                <span title="Send TestRequest"><button onclick="sendCommand('${exchange.acronym}', 'testRequest')">🧪</button></span>
                <span title="Send ResendRequest"><button onclick="sendCommand('${exchange.acronym}', 'resendRequest')">🔁</button></span>
              </div>
            </td>
        `;
        tbody.appendChild(row);
    });
}

function formatAge(ageMillis) {
  if (ageMillis == null || isNaN(ageMillis)) return "—";
  if (ageMillis < 1000) return `${ageMillis}ms`;
  return `${(ageMillis / 1000).toFixed(1)}s`;
}

function sendCommand(acronym, action) {
    fetch(`/control`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ acronym, action })
    })
    .then(res => {
        if (!res.ok) throw new Error("Command failed");
        console.log(`Sent ${action} to ${acronym}`);
    })
    .catch(err => console.error("Control error:", err));
}

function updateClock() {
    const now = new Date();
    const formatted = now.toLocaleTimeString("en-US", {
        hour12: false,
        hour: "2-digit",
        minute: "2-digit",
        second: "2-digit"
    }) + "." + String(now.getMilliseconds()).padStart(3, "0");

    document.getElementById("currentTime").textContent = formatted;
}

setInterval(updateClock, 100);
updateClock();

function togglePolling() {
    pollingActive = !pollingActive;
    const btn = document.getElementById("togglePolling");
    btn.textContent = pollingActive ? "Pause" : "Resume";
    btn.style.backgroundColor = pollingActive ? "#e0e0e0" : "#cc3333";
    btn.style.color = pollingActive ? "#333" : "#fff";
}
// Set initial button style without toggling state
const btn = document.getElementById("togglePolling");
btn.textContent = "Pause";
btn.style.backgroundColor = "#e0e0e0";
btn.style.color = "#333";

function pollStatus() {
    if (!pollingActive) return;
    fetch("/status-data")
        .then(res => res.json())
        .then(data => {
            updateExchangeTable(data);
            updateLastUpdated();
        })
        .catch(err => console.error("Status fetch failed:", err));
}

function updateLastUpdated() {
    const now = new Date();
    const formatted = now.toLocaleTimeString("en-US", {
        hour12: false,
        hour: "2-digit",
        minute: "2-digit",
        second: "2-digit"
    }) + "." + String(now.getMilliseconds()).padStart(3, "0");
    document.getElementById("lastUpdated").textContent = formatted;
}

document.getElementById("togglePolling").addEventListener("click", togglePolling);

pollingInterval = setInterval(pollStatus, 1000);
pollStatus(); // initial call

document.getElementById("disconnectAll").addEventListener("click", () => {
    fetch("/control", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ action: "disconnectAll" })
    })
    .then(res => {
        if (!res.ok) throw new Error("Disconnect All failed");
        console.log("All exchanges disconnected");
    })
    .catch(err => console.error("Disconnect All error:", err));
});

document.getElementById("connectAll").addEventListener("click", () => {
    fetch("/control", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ action: "connectAll" })
    })
    .then(res => {
        if (!res.ok) throw new Error("Connect All failed");
        console.log("All exchanges connected");
    })
    .catch(err => console.error("Connect All error:", err));
});