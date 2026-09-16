# 📈 K-Memory

### A-Share Market Data Platform

> **A modern full-stack platform for collecting, storing, searching and visualizing A-share market data.**
>
> Built with **Spring Boot · Vue 3 · MySQL · Tushare Pro · ECharts**

<p align="center">
  <img src="https://img.shields.io/badge/Java-17+-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white" />
  <img src="https://img.shields.io/badge/Spring%20Boot-3.x-6DB33F?style=for-the-badge&logo=springboot&logoColor=white" />
  <img src="https://img.shields.io/badge/Vue.js-3.x-42B883?style=for-the-badge&logo=vuedotjs&logoColor=white" />
  <img src="https://img.shields.io/badge/MySQL-8.x-4479A1?style=for-the-badge&logo=mysql&logoColor=white" />
  <img src="https://img.shields.io/badge/Vite-646CFF?style=for-the-badge&logo=vite&logoColor=white" />
  <img src="https://img.shields.io/badge/ECharts-AA344D?style=for-the-badge&logo=apacheecharts&logoColor=white" />
</p>

<p align="center">
  <a href="#-overview">Overview</a> •
  <a href="#-features">Features</a> •
  <a href="#-architecture">Architecture</a> •
  <a href="#-tech-stack">Tech Stack</a> •
  <a href="#-quick-start">Quick Start</a> •
  <a href="#-api">API</a> •
  <a href="#-roadmap">Roadmap</a>
</p>

---

## ✨ Overview

**K-Memory** is a full-stack A-share market data platform designed to provide a complete workflow from **market-data acquisition to persistent storage and interactive visualization**.

The application integrates the **Tushare Pro API** as its external data source, uses **Spring Boot** to handle business logic and RESTful APIs, stores market data in **MySQL**, and provides a modern **Vue 3 + Vite + ECharts** interface for stock search and K-line visualization.

The project focuses on practical full-stack engineering rather than simply displaying static data.

```text
                 ┌─────────────────────┐
                 │    Tushare Pro API   │
                 │ Stock / Daily K-Line │
                 └──────────┬──────────┘
                            │
                            ▼
              ┌─────────────────────────┐
              │      Spring Boot        │
              │                         │
              │ Controller → Service    │
              │        → Repository     │
              └────────────┬────────────┘
                           │
                    ┌──────┴──────┐
                    ▼             ▼
              ┌──────────┐   ┌────────────┐
              │  MySQL   │   │ Local Cache│
              │ Database │   │   TSV      │
              └─────┬────┘   └────────────┘
                    │
                    │ REST / JSON
                    ▼
              ┌─────────────────────────┐
              │       Vue 3 + Vite      │
              │                         │
              │ Search · Collection     │
              │ Stock Detail · Charts   │
              └────────────┬────────────┘
                           │
                           ▼
                    📊 ECharts
                 Interactive K-Line
```

---

# 🚀 Features

### 🔍 Stock Search

Search A-share stocks using:

* 6-digit stock codes
* Chinese stock names
* Local cached stock list

---

### 📥 Market Data Collection

Retrieve historical daily K-line data from Tushare Pro and persist the results into MySQL.

Users can specify the amount of historical data to collect.

Example:

```json
{
  "code": "600519",
  "days": 250
}
```

---

### 💾 Persistent Data Storage

Market data is stored in a relational database using:

```text
Spring Data JPA
        ↓
Hibernate
        ↓
MySQL
```

This allows the frontend to work with locally persisted market data instead of relying entirely on external API requests.

---

### 📊 Interactive K-Line Visualization

Historical market data is converted into frontend-friendly JSON and visualized through **Apache ECharts**.

The frontend supports an interactive workflow:

```text
Search Stock
     ↓
Select Stock
     ↓
Collect Historical Data
     ↓
Query Local Database
     ↓
Render K-Line Chart
```

---

### ⚡ API Rate-Limit Handling

Tushare Pro's `stock_basic` interface has request-frequency restrictions for free accounts.

K-Memory therefore implements a local caching mechanism:

```text
Request Stock List
       ↓
Check Local Cache
       ↓
 ┌─────┴─────┐
 │           │
Valid       Expired
 │           │
 ▼           ▼
Return    Request API
Cache         │
              ▼
         Update Cache
```

The local stock-list cache is valid for **24 hours**.

When the external API is unavailable or rate-limited, the application can fall back to the existing cache.

---

### 🗑️ Data Management

The backend provides APIs for:

* Querying recorded stocks
* Querying individual stock details
* Querying historical daily data
* Collecting new market data
* Deleting stocks and their associated data

---

# 🏗️ Architecture

K-Memory adopts a classic **frontend–backend separation architecture**.

```text
┌───────────────────────────────────────────────┐
│                  Frontend                     │
│                                               │
│             Vue 3 + Vite + ECharts            │
│                                               │
│  Search │ Stock List │ Detail │ K-Line Chart │
└───────────────────────┬───────────────────────┘
                        │
                  REST / JSON
                        │
                        ▼
┌───────────────────────────────────────────────┐
│                  Backend                      │
│                                               │
│                 Spring Boot                  │
│                                               │
│ Controller → Service → Repository             │
│       │           │            │              │
│       └───────────┴────────────┘              │
│                    │                          │
│             Tushare RestClient                │
└───────────────┬───────────────────┬───────────┘
                │                   │
                ▼                   ▼
        ┌──────────────┐     ┌──────────────┐
        │    MySQL     │     │ Local Cache  │
        │              │     │              │
        │ Stock Data   │     │ TSV File     │
        └──────────────┘     └──────────────┘
```

---

# 🛠️ Tech Stack

## Backend

| Technology          | Purpose                       |
| ------------------- | ----------------------------- |
| **Java 17+**        | Main programming language     |
| **Spring Boot**     | Backend application framework |
| **Spring Web**      | RESTful API                   |
| **Spring Data JPA** | Persistence layer             |
| **Hibernate**       | ORM                           |
| **RestClient**      | External API communication    |
| **Maven**           | Dependency management & build |

## Frontend

| Technology     | Purpose                         |
| -------------- | ------------------------------- |
| **Vue 3**      | Frontend framework              |
| **Vite**       | Development server & build tool |
| **JavaScript** | Application logic               |
| **ECharts**    | Financial data visualization    |
| **HTML / CSS** | UI implementation               |

## Database & Data Source

| Technology      | Purpose                  |
| --------------- | ------------------------ |
| **MySQL**       | Persistent storage       |
| **Tushare Pro** | A-share market data      |
| **TSV Cache**   | Local stock-list caching |

---

# 📂 Project Structure

```text
k-memory-repository/
│
├── backend/
│   ├── src/
│   │   └── main/
│   │       ├── java/
│   │       │   └── ...
│   │       └── resources/
│   │           └── application.properties
│   │
│   ├── pom.xml
│   ├── mvnw
│   └── mvnw.cmd
│
├── frontend/
│   ├── src/
│   │   ├── components/
│   │   ├── views/
│   │   └── ...
│   ├── package.json
│   └── vite.config.*
│
├── stock_list_cache.tsv
│
└── README.md
```

---

# 🔌 REST API

|  Method  | Endpoint                          | Description                   |
| :------: | --------------------------------- | ----------------------------- |
|   `GET`  | `/api/stocks`                     | Get recorded stocks           |
|   `GET`  | `/api/stocks/search?keyword=`     | Search stocks                 |
|  `POST`  | `/api/stocks/collect`             | Collect & persist market data |
|   `GET`  | `/api/stocks/{code}`              | Get stock details             |
|   `GET`  | `/api/stocks/{code}/daily?limit=` | Get historical K-line data    |
| `DELETE` | `/api/stocks/{code}`              | Delete stock data             |

### Example

Collect 250 days of data for Kweichow Moutai:

```http
POST /api/stocks/collect
Content-Type: application/json
```

```json
{
  "code": "600519",
  "days": 250
}
```

---

# ⚙️ Quick Start

## 1. Requirements

Make sure you have:

```text
JDK 17+
Node.js 18+
MySQL
Tushare Pro Account
```

---

## 2. Clone

```bash
git clone https://github.com/yehuohua/k-memory-repository.git

cd k-memory-repository
```

---

## 3. Configure Database & API

Open:

```text
backend/src/main/resources/application.properties
```

Configure your:

```properties
spring.datasource.username=YOUR_USERNAME
spring.datasource.password=YOUR_PASSWORD
tushare.token=YOUR_TUSHARE_TOKEN
```
## 4. Start Backend

### Windows

```bash
cd backend

mvnw.cmd spring-boot:run
```

### macOS / Linux

```bash
cd backend

./mvnw spring-boot:run
```

Default backend address:

```text
http://localhost:8081
```

Test the API:

```text
http://localhost:8081/api/stocks
```

---

## 5. Start Frontend

Open another terminal:

```bash
cd frontend

npm install

npm run dev
```

Then open the local URL provided by Vite.

---

# 🧠 Engineering Highlights

## 01 — Full Data Pipeline

The project implements an end-to-end data pipeline:

```text
External Data
     ↓
API Integration
     ↓
Data Processing
     ↓
Persistence
     ↓
REST API
     ↓
Frontend
     ↓
Visualization
```

This provides a practical demonstration of how a full-stack application processes external structured data.

---

## 02 — Frontend / Backend Separation

The application separates:

```text
Presentation Layer
        ↓
REST API
        ↓
Business Layer
        ↓
Persistence Layer
```

This makes the system easier to maintain and provides a foundation for future expansion.

---

## 03 — External API Integration

Spring Boot's HTTP client is used to communicate with Tushare Pro.

The backend is responsible for:

* Request construction
* API communication
* Response processing
* Data transformation
* Persistence

---

## 04 — Database Persistence

Instead of displaying API responses directly, the project persists market data into MySQL.

This allows:

* Local historical-data queries
* Reduced repeated API requests
* More flexible future analysis
* Independent frontend data access

---

## 05 — Cache Strategy

The project introduces a lightweight caching mechanism to reduce unnecessary requests to the external stock-list API.

```text
Cache Hit
   ↓
Return Cached Data

Cache Miss
   ↓
Request Tushare
   ↓
Update Local Cache
```

This is particularly useful when working with third-party APIs that have request-frequency limitations.

---

# 📈 Future Roadmap

The current architecture provides a foundation for further financial-data and AI-related features.

### Data & Analytics

* [ ] MA / EMA indicators
* [ ] MACD
* [ ] RSI
* [ ] Volume analysis
* [ ] Historical performance analysis
* [ ] More financial-data endpoints

### Portfolio

* [ ] Personal watchlist
* [ ] Portfolio tracking
* [ ] Position management
* [ ] Profit & loss calculation

### Engineering

* [ ] Scheduled data synchronization
* [ ] Pagination
* [ ] Authentication & authorization
* [ ] Docker Compose deployment
* [ ] Automated testing
* [ ] GitHub Actions CI/CD

### AI

* [ ] AI-powered market-data summarization
* [ ] Natural-language stock queries
* [ ] AI-assisted financial-data analysis
* [ ] Intelligent chart interpretation

---

# 🎯 Why This Project?

K-Memory was created as a practical full-stack engineering project to explore how a real data-driven application is designed and implemented.

The project covers several important areas:

```text
Java
  │
  ├── Spring Boot
  │
  ├── RESTful API
  │
  ├── Database Persistence
  │
  └── External API Integration
          │
          ▼
        MySQL
          │
          ▼
      Vue 3 / Vite
          │
          ▼
       ECharts
          │
          ▼
   Data Visualization
```

It can also serve as a foundation for future development in:

* 📊 Financial data platforms
* 🤖 AI-assisted data analysis
* 📈 Quantitative analysis
* 💼 Portfolio management
* 🌐 Full-stack web applications

---

# 🔐 Security

Please make sure sensitive credentials are never committed to the repository.

Sensitive information includes:

```text
API Tokens
Database Passwords
Private Keys
Access Tokens
Environment Secrets
```

Recommended approach:

```text
Environment Variables
        ↓
Spring Boot Configuration
        ↓
Application
```

---

# 📌 Project Status

**Status:** 🚧 Active Development

The core market-data collection, persistence, REST API and frontend visualization workflow is implemented.

The project is designed to evolve from a basic market-data application into a more comprehensive **financial data + analytics + AI** platform.

---

# 📄 License

This project is intended for **learning and personal development**.

When using Tushare Pro data, please comply with its applicable terms of service, API restrictions and data-usage requirements.

---

<p align="center">

### Built with ☕ Java · 🌱 Spring Boot · 💚 Vue · 🐬 MySQL · 📊 ECharts

**K-Memory** — Turning market data into an interactive experience.

</p>
