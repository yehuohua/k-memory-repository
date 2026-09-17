# 📈 K-Memory

### A股市场数据平台

> 基于 **Spring Boot + Vue 3 + MySQL + Tushare Pro + ECharts** 构建的全栈 A 股市场数据平台，实现股票搜索、行情数据采集、数据库持久化以及 K 线数据可视化。

<p align="center">
  <img src="https://img.shields.io/badge/Java-17+-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white" />
  <img src="https://img.shields.io/badge/Spring%20Boot-3.x-6DB33F?style=for-the-badge&logo=springboot&logoColor=white" />
  <img src="https://img.shields.io/badge/Vue.js-3.x-42B883?style=for-the-badge&logo=vuedotjs&logoColor=white" />
  <img src="https://img.shields.io/badge/MySQL-8.x-4479A1?style=for-the-badge&logo=mysql&logoColor=white" />
  <img src="https://img.shields.io/badge/Vite-646CFF?style=for-the-badge&logo=vite&logoColor=white" />
  <img src="https://img.shields.io/badge/ECharts-AA344D?style=for-the-badge&logo=apacheecharts&logoColor=white" />
</p>

<p align="center">
  <a href="#-项目简介">项目简介</a> •
  <a href="#-核心功能">核心功能</a> •
  <a href="#-系统架构">系统架构</a> •
  <a href="#-技术栈">技术栈</a> •
  <a href="#-快速开始">快速开始</a> •
  <a href="#-rest-api">REST API</a> •
  <a href="#-未来规划">未来规划</a>
</p>

---

## ✨ 项目简介

**K-Memory** 是一个面向 A 股市场数据的全栈 Web 应用，主要用于实现从**行情数据获取、数据处理、数据库存储到前端可视化展示**的完整数据流程。

项目以 **Tushare Pro** 作为外部行情数据源，通过 **Spring Boot** 构建后端服务和 RESTful API，使用 **MySQL** 对股票及历史行情数据进行持久化存储，并采用 **Vue 3 + Vite + ECharts** 构建前端数据展示界面。

项目并非简单的数据展示 Demo，而是完整覆盖了：

**第三方 API → 后端数据采集 → 数据持久化 → REST API → 前端展示 → K 线可视化**

整个数据处理流程。

```text
                 ┌─────────────────────┐
                 │    Tushare Pro API   │
                 │ Stock / Daily K-Line │
                 └──────────┬──────────┘
                            │
                            ▼
              ┌─────────────────────────┐
              │      Spring Boot       │
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
              │ Search │ Collection     │
              │ Stock Detail · K-Line  │
              └────────────┬────────────┘
                           │
                           ▼
                    📊 ECharts
                 Interactive K-Line
```

---

# 🚀 核心功能

### 🔍 股票搜索

支持根据以下信息搜索 A 股股票：

* 6 位股票代码
* 中文股票名称
* 本地缓存的股票列表

用户可以快速定位目标股票并进入行情数据页面。

---

### 📥 行情数据采集

通过 Tushare Pro 获取股票历史日 K 行情数据，并将数据处理后持久化到 MySQL 数据库。

用户可以根据需求指定需要采集的历史数据天数。

例如：

```json
{
  "code": "600519",
  "days": 250
}
```

---

### 💾 数据持久化

项目使用 **Spring Data JPA + Hibernate + MySQL** 构建数据持久化层。

整体数据存储流程：

```text
Spring Data JPA
        ↓
Hibernate ORM
        ↓
MySQL
```

通过 ORM 将 Java 对象与数据库表进行映射，降低数据库操作与业务代码之间的耦合。

---

### 📊 K 线数据可视化

历史行情数据经过后端处理后，通过 REST API 返回给 Vue 3 前端，并使用 **Apache ECharts** 进行可视化。

完整操作流程：

```text
搜索股票
   ↓
选择股票
   ↓
采集历史数据
   ↓
查询 MySQL
   ↓
获取历史行情
   ↓
ECharts 绘制 K 线
```

用户可以更加直观地查看股票历史价格变化。

---

### ⚡ API 请求限制与缓存机制

由于 Tushare Pro 部分接口存在请求频率限制，项目针对股票基础信息接口设计了本地缓存机制。

```text
请求股票列表
       ↓
检查本地缓存
       ↓
 ┌─────┴─────┐
 │           │
缓存有效    缓存过期
 │           │
 ▼           ▼
返回缓存    请求 Tushare
              │
              ▼
          更新本地缓存
```

股票列表缓存有效期为 **24 小时**。

当接口受到请求频率限制时，可以使用已有缓存数据，从而降低对第三方 API 的依赖。

---

### 🗑️ 数据管理

后端提供完整的股票数据管理接口，包括：

* 查询已经记录的股票
* 查询指定股票详情
* 查询历史日 K 数据
* 采集新的行情数据
* 删除股票及相关行情数据

---

# 🏗️ 系统架构

K-Memory 采用经典的**前后端分离架构**。

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

# 🛠️ 技术栈

## 后端

| 技术                  | 用途           |
| ------------------- | ------------ |
| **Java 17+**        | 后端主要开发语言     |
| **Spring Boot**     | 后端应用开发框架     |
| **Spring Web**      | RESTful API  |
| **Spring Data JPA** | 数据持久化        |
| **Hibernate**       | ORM 框架       |
| **RestClient**      | 第三方 API 接口调用 |
| **Maven**           | 项目构建与依赖管理    |

## 前端

| 技术             | 用途           |
| -------------- | ------------ |
| **Vue 3**      | 前端开发框架       |
| **Vite**       | 前端开发服务器及构建工具 |
| **JavaScript** | 前端业务逻辑       |
| **ECharts**    | 数据可视化        |
| **HTML / CSS** | 页面结构与样式      |

## 数据库与数据源

| 技术              | 用途        |
| --------------- | --------- |
| **MySQL**       | 市场数据持久化存储 |
| **Tushare Pro** | A 股市场数据接口 |
| **TSV Cache**   | 股票列表本地缓存  |

---

# 📂 项目结构

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

# 🚀 快速开始

## 1. 环境要求

运行项目之前，请确保已经安装：

```text
JDK 17+
Node.js 18+
MySQL
Tushare Pro 账号
```

---

## 2. 克隆项目

```bash
git clone https://github.com/yehuohua/k-memory-repository.git

cd k-memory-repository
```

---

## 3. 配置数据库和 API

打开：

```text
backend/src/main/resources/application.properties
```

配置 MySQL 和 Tushare Pro：

```properties
spring.datasource.username=YOUR_USERNAME
spring.datasource.password=YOUR_PASSWORD

tushare.token=YOUR_TUSHARE_TOKEN
```

> ⚠️ **安全提示：不要将真实 API Token、数据库密码或其他敏感信息提交到 GitHub。**

推荐在本地开发环境中使用环境变量或未被 Git 跟踪的本地配置文件保存敏感信息。

---

## 4. 启动后端

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

默认后端端口：

```text
http://localhost:8081
```

可以通过以下地址测试 API：

```text
http://localhost:8081/api/stocks
```

---

## 5. 启动前端

重新打开一个终端：

```bash
cd frontend

npm install

npm run dev
```

启动成功后，打开 Vite 终端中显示的本地地址即可访问前端页面。

---

# 🔌 REST API

|    方法    | 接口                                | 功能          |
| :------: | --------------------------------- | ----------- |
|   `GET`  | `/api/stocks`                     | 获取已经记录的股票   |
|   `GET`  | `/api/stocks/search?keyword=`     | 根据代码或名称搜索股票 |
|  `POST`  | `/api/stocks/collect`             | 采集并保存行情数据   |
|   `GET`  | `/api/stocks/{code}`              | 获取股票详情      |
|   `GET`  | `/api/stocks/{code}/daily?limit=` | 获取历史日 K 数据  |
| `DELETE` | `/api/stocks/{code}`              | 删除股票及相关数据   |

### 数据采集示例

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

# 🧠 项目技术亮点

## 01 — 完整的数据处理链路

项目实现了从第三方数据源到前端可视化的完整数据链路：

```text
第三方行情数据
      ↓
API 接口调用
      ↓
后端数据处理
      ↓
MySQL 数据持久化
      ↓
REST API
      ↓
Vue 3
      ↓
ECharts
      ↓
行情数据可视化
```

能够较完整地体现一个数据驱动型 Web 应用的基本开发流程。

---

## 02 — 前后端分离

项目采用前后端分离设计：

```text
前端展示层
    ↓
REST API
    ↓
业务逻辑层
    ↓
数据持久化层
```

前端主要负责页面展示和用户交互，后端负责业务逻辑、数据处理以及数据库操作。

这种架构也方便后续继续扩展移动端、AI 服务或其他数据分析模块。

---

## 03 — 第三方 API 集成

后端通过 Spring Boot 的 HTTP Client 与 Tushare Pro 进行通信。

主要负责：

* 构造 API 请求
* 发送 HTTP 请求
* 处理 API 返回结果
* 转换行情数据
* 数据持久化

---

## 04 — 数据库持久化

项目没有简单地将第三方 API 返回的数据直接展示，而是将行情数据保存到 MySQL。

这样可以实现：

* 本地历史数据查询
* 减少重复 API 请求
* 为后续数据分析提供基础
* 降低前端对第三方 API 的直接依赖

---

## 05 — 本地缓存策略

针对第三方 API 的访问限制，项目引入轻量级本地缓存机制。

```text
检查缓存
   ↓
 ┌─────────────┐
 │             │
缓存命中      缓存未命中
 │             │
 ▼             ▼
返回数据      请求 API
               │
               ▼
           更新缓存
```

这种设计能够减少不必要的第三方 API 请求，提高应用在开发环境下的稳定性。

---

# 🔮 未来规划

目前项目已经实现基础的行情数据采集、数据库存储、REST API 和 K 线可视化功能。

后续计划进一步扩展。

### 📊 数据分析

* [ ] 增加 MA / EMA 技术指标
* [ ] 增加 MACD
* [ ] 增加 RSI
* [ ] 增加成交量分析
* [ ] 增加历史收益率分析
* [ ] 扩展更多金融数据接口

### 💼 个人投资组合

* [ ] 自选股票
* [ ] 个人投资组合管理
* [ ] 持仓管理
* [ ] 收益率计算
* [ ] 盈亏统计

### ⚙️ 工程能力

* [ ] 定时自动同步行情数据
* [ ] 分页查询
* [ ] 用户登录与权限管理
* [ ] Docker Compose 一键部署
* [ ] 自动化测试
* [ ] GitHub Actions CI/CD

### 🤖 AI 能力

* [ ] AI 行情数据摘要
* [ ] 自然语言股票查询
* [ ] AI 辅助金融数据分析
* [ ] AI 图表分析
* [ ] 智能行情报告生成

---

# 🎯 项目目标

K-Memory 旨在通过一个完整的数据驱动型项目，实践和理解现代 Web 应用从**数据获取到最终可视化展示**的完整开发流程。

通过这个项目，综合应用了：

```text
Java
  │
  ├── Spring Boot
  │
  ├── RESTful API
  │
  ├── 数据库持久化
  │
  └── 第三方 API 集成
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
      数据可视化
```

项目后续可以继续向以下方向扩展：

* 📊 金融数据分析平台
* 🤖 AI 金融数据分析
* 📈 量化分析工具
* 💼 个人投资组合管理
* 🌐 全栈数据应用

---

# 🔐 安全说明

请确保敏感信息不会被提交到 GitHub。

敏感信息包括：

```text
API Token
数据库密码
Private Key
Access Token
环境变量中的密钥
```

推荐采用：

```text
环境变量
   ↓
Spring Boot 配置
   ↓
应用程序
```

---

# 📌 项目状态

**开发状态：** 🚧 持续开发中

目前已经完成：

* ✅ A 股股票搜索
* ✅ Tushare Pro 行情数据获取
* ✅ 股票历史数据采集
* ✅ MySQL 数据持久化
* ✅ RESTful API
* ✅ Vue 3 前端
* ✅ ECharts K 线可视化
* ✅ 股票列表本地缓存
* ✅ 股票数据删除与管理

后续将继续围绕**金融数据分析 + AI + 全栈应用**进行扩展。

---

# 📄 License

本项目主要用于**个人学习、技术实践与项目开发**。

使用 Tushare Pro 数据时，请遵守其相关服务条款、API 调用限制以及数据使用规范。

---

<p align="center">

### ☕ Java · 🌱 Spring Boot · 💚 Vue · 🐬 MySQL · 📊 ECharts

**K-Memory**

一个从数据采集到可视化的 A 股市场数据平台。

</p>
