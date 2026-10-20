# 📈 Spring Boot Stock Tracker & Portfolio Analytics API

[![Java](https://img.shields.io/badge/Java-17%2B%20%2F%2025-ED8B00?style=flat&logo=openjdk&logoColor=white)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x%20%2F%204.x-6DB33F?style=flat&logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![H2 Database](https://img.shields.io/badge/Database-H2%20File%20DB-blue?style=flat)](https://www.h2database.com/)
[![License](https://img.shields.io/badge/License-MIT-lightgrey.svg)](LICENSE)

A high-performance financial tracking and portfolio analytics REST API built with **Spring Boot** and **Alpha Vantage**. Features real-time price quotes, historical OHLCV chart data, symbol autocomplete, mark-to-market portfolio valuation with unrealized P&L, price trigger alerts, Spring caching, and an institutional Bloomberg-style dark-mode web dashboard.

---

## 🚀 Key Features

- **Live Market Quotes & Key Statistics**: Real-time prices, percentage changes, daily high/low, open, previous close, and trading volumes.
- **Interactive OHLCV Charts**: Historical daily performance (adjustable lookback window up to 100 days) visualized with Chart.js.
- **Smart Ticker Search**: Instant ticker symbol autocomplete powered by Alpha Vantage search.
- **Watchlist & Favorites**: Persisted stock watchlists with parallel asynchronous price enrichment via `CompletableFuture`.
- **Portfolio Management & P&L Analytics**: Tracks share quantities, purchase prices, aggregate cost basis, current market valuations, and unrealized profit/loss.
- **Price Threshold Alerts**: Setup custom `ABOVE` or `BELOW` price alerts with automated trigger state tracking.
- **Resilient Caching**: Spring Cache abstraction reduces external API latency and guards against third-party rate limits.
- **Built-in Web Terminal**: Responsive, zero-dependency dark-mode dashboard served straight from Spring Boot static resources.
- **Embedded Database**: H2 file database (`./data/stockdb`) with `AUTO_SERVER=TRUE` and web console access.

---

## 🛠️ Tech Stack

| Layer | Technologies |
|---|---|
| **Runtime & Framework** | Java 17+ / 25, Spring Boot, Spring WebMVC, Spring Data JPA |
| **Reactive HTTP** | Spring WebFlux `WebClient` |
| **Caching** | Spring Cache (`ConcurrentMapCache`) |
| **Database & ORM** | H2 Database Engine, Hibernate |
| **Market Data Provider** | [Alpha Vantage API](https://www.alphavantage.co/) |
| **Frontend Dashboard** | Vanilla HTML5, Modern CSS, ES6 JavaScript, [Chart.js](https://www.chartjs.org/) |
| **Build & Utilities** | Maven Wrapper, Project Lombok |

---

## 📂 API Reference

Base URL: `http://localhost:8081/api/v1`

### 1. Market Data & Watchlist (`/stocks`)

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/stocks/search?query={query}` | Search stocks and ETFs by ticker or name |
| `GET` | `/stocks/{symbol}` | Get real-time quote and daily stats for a ticker |
| `GET` | `/stocks/{symbol}/overview` | Get fundamental overview or fallback quote data |
| `GET` | `/stocks/{symbol}/history?days=30` | Get daily OHLCV historical time series |
| `GET` | `/stocks/favourites` | List all saved watchlist stocks with live prices |
| `POST` | `/stocks/favourites` | Add a stock ticker to watchlist |
| `DELETE` | `/stocks/favourites/{symbol}` | Remove a stock ticker from watchlist |

#### Add to Watchlist
```bash
curl -X POST http://localhost:8081/api/v1/stocks/favourites \
  -H "Content-Type: application/json" \
  -d '{"symbol": "AAPL"}'
```

---

### 2. Portfolio Management (`/portfolio`)

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/portfolio` | List all holdings with live mark-to-market prices and P&L |
| `GET` | `/portfolio/summary` | Aggregate portfolio summary (Total Cost, Value, P&L, P&L %) |
| `POST` | `/portfolio` | Record a new stock position |
| `DELETE` | `/portfolio/{id}` | Delete a portfolio holding by ID |

#### Add Portfolio Position
```bash
curl -X POST http://localhost:8081/api/v1/portfolio \
  -H "Content-Type: application/json" \
  -d '{
    "symbol": "MSFT",
    "quantity": 15.0,
    "buyPrice": 410.50,
    "buyDate": "2026-03-15"
  }'
```

#### Portfolio Summary Response Example
```json
{
  "totalInvested": 6157.50,
  "totalCurrentValue": 6450.00,
  "totalProfitLoss": 292.50,
  "totalProfitLossPercentage": 4.75,
  "holdingsCount": 1
}
```

---

### 3. Price Alerts (`/alerts`)

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/alerts` | Retrieve all active and triggered price alerts |
| `POST` | `/alerts` | Create a new price threshold alert |
| `POST` | `/alerts/check` | Scan market prices and trigger matching alerts |
| `DELETE` | `/alerts/{id}` | Delete an alert by ID |

#### Create Price Alert
```bash
curl -X POST http://localhost:8081/api/v1/alerts \
  -H "Content-Type: application/json" \
  -d '{
    "symbol": "NVDA",
    "targetPrice": 140.00,
    "direction": "ABOVE"
  }'
```
*Supported `direction` values: `ABOVE`, `BELOW`*

---

## ⚡ Getting Started

### Prerequisites
- **JDK 17+** (JDK 25 supported)
- **Maven 3.8+** (or use bundled `./mvnw`)
- **Alpha Vantage API Key** (Free tier available at [alphavantage.co](https://www.alphavantage.co/support/#api-key))

### 1. Clone the Repository
```bash
git clone https://github.com/soul-aryann/stock-tracker.git
cd stock-tracker
```

### 2. Configure API Key & Properties
Open [`src/main/resources/application.properties`](file:///Users/soularyan/Desktop/Coding%20Stuff/stock-tracker/src/main/resources/application.properties) and update your Alpha Vantage API key:

```properties
server.port=8081
alpha.vantage.api.key=YOUR_API_KEY_HERE
alpha.vantage.base.url=https://www.alphavantage.co/query

# H2 File Database
spring.datasource.url=jdbc:h2:file:./data/stockdb;AUTO_SERVER=TRUE
spring.datasource.username=sa
spring.datasource.password=
spring.jpa.hibernate.ddl-auto=update

# H2 Web Console
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console

# Caching
spring.cache.type=simple
```

### 3. Build & Run
```bash
# Using the Maven wrapper
./mvnw clean spring-boot:run
```

The application starts on port **8081**:
- **Web Terminal**: [http://localhost:8081](http://localhost:8081)
- **H2 Database Console**: [http://localhost:8081/h2-console](http://localhost:8081/h2-console) (JDBC URL: `jdbc:h2:file:./data/stockdb`)

### 4. Run Automated Tests
```bash
./mvnw test
```

---

## 🛡️ Architecture & Design Decisions

- **Parallel Quote Fetching**: Watchlist and portfolio endpoints fetch quotes concurrently using `CompletableFuture.supplyAsync()` to reduce total response time.
- **Rate Limit Safeguards**: Alpha Vantage free tiers impose a 5 requests/minute threshold. Responses are cached via `@Cacheable` on `stockQuotes`, `stockHistories`, and `stockOverviews`.
- **Isolated Test DB**: Unit and integration tests leverage an isolated in-memory H2 database (`jdbc:h2:mem:testdb`) to avoid mutating local persistent file storage.
