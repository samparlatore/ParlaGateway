# ParlAquatics Gateway

A modular, thread-safe gateway engine for simulating and connecting to multiple financial exchanges using configurable protocols like FIX and JSON. Built for flexibility, observability, and realism — designed and developed by Sam Parlatore.

## 🚀 Overview

ParlAquatics Gateway is a Java-based backend system that:

- Connects to multiple simulated or real exchange endpoints
- Dispatches each connection to a protocol-specific handler
- Supports FIX, JSON, and pluggable protocols via a factory-based architecture
- Uses config-driven concurrency, retry logic, and connection windows
- Designed for extensibility, testability, and production-grade robustness

## 🧩 Architecture

```text
+------------------+
|   GatewayCore    |
|------------------|
| - Thread pool    |
| - Retry logic    |
| - Config loader  |
+--------+---------+
         |
         v
+--------------------------+
| ExchangeHandlerFactory   |
+--------------------------+
         |
         v
+------------------+     +------------------+
| FixProtocolHandler|     |JsonProtocolHandler|
+------------------+     +------------------+
         |
         v
   [Socket Connection]
```
## ⚙️ Configuration

Exchange behavior is defined in `NmsExchangeConfig` objects, loaded from a config file or database. Each config includes:

- `name`
- `acronym`: Unique exchange ID
- `ipAddress` 
- `port`: Target endpoint
- `handlerName`: Protocol handler to use (`fix`, `json`, etc.)
- `connectionRetries`, 
- `retryInterval`, 
- `startTime`, 
- `endTime`: 
- `Connection policy`

Global gateway settings (e.g., thread pool model) are loaded from a `Properties` file:

```properties
gateway.threadingModel=fixed
gateway.threadPoolSize=12
gateway.retryBackoffStrategy=exponential
gateway.retrySchedulerThreads=4
```

## 🧪 Handlers & Factories
- Handlers implement the `ExchangeHandler` interface:
- Each handler is created by a dedicated factory:
- This ensures thread-safe, config-aware instantiation per exchange.

# 🛠️ Supported Protocols
- ✅ fix — via QuickFIX/J (FIX 4.2, 4.4 supported)
- ✅ json — simple echo-style JSON handler
- 🧩 Add your own by implementing `ExchangeHandler` and registering a factory

# 🧵 Threading Model
Configurable via :
- fixed	 (default)
- cached	
- scheduled	
- workStealing	

Each exchange runs in its own thread, with retry logic and jittered backoff.

## 📦 Dependencies
```xml
<dependency>
  <groupId>org.quickfixj</groupId>
  <artifactId>quickfixj-core</artifactId>
  <version>2.0.0</version>
</dependency>
<dependency>
  <groupId>org.slf4j</groupId>
  <artifactId>slf4j-log4j12</artifactId>
  <version>1.7.22</version>
</dependency>
```

Add only the FIX message versions you need (e.g., quickfix-messages-fix42, fix44, etc.).

## 🧪 Testing
Use DefaultExchangeServer to simulate echo servers or stub protocols. You can also spin up local servers to test connection logic and retry behavior.

## 🧠 Design Philosophy
- Thread-safe by design: No shared mutable state across handlers
- Config-driven: Behavior is defined by  and  files
- Extensible: Add new protocols with minimal boilerplate
- Observable: Logs connection attempts, retries, and handler activity
## 👨‍💻 Author
Built by Sam Parlatore
GitHub: github.com/samparlatore


