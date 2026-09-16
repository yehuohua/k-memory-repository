# 股票数据中心

一个精简的 A 股行情记录与展示工具：Spring Boot 后端直连 Tushare Pro 行情接口抓取日 K 数据并存入 MySQL，Vue3 前端以科技风界面展示。

## 架构

```
Tushare Pro 行情接口（daily 日K / stock_basic 股票列表，需 token）
        │  Spring Boot 直连（RestClient）
        ▼
Spring Boot 后端 (localhost:8081) ── 持久化 ──► MySQL (stock 库)
        │  REST JSON /api/*
        ▼
Vue3 + Vite 前端 (localhost:5173) ── 展示：列表 / 搜索 / 采集 / K 线图
```

## 目录结构

```
stock/
├── backend/    Spring Boot（Java 17，Maven Wrapper 自带下载 Maven）
├── frontend/   Vue3 + Vite + ECharts
└── README.md
```

## 前置条件

- JDK 17+
- Node.js 18+
- 正在运行的 MySQL
- Tushare Pro 账号 token（已配置在 `application.properties` 的 `tushare.token`）

## 启动后端

```bash
cd backend
./mvnw spring-boot:run          # Windows 用 mvnw.cmd
```

- 首次运行会通过 Maven Wrapper 自动下载 Maven 和依赖（已配置阿里云镜像加速）。
- 数据库 `stock` 会自动创建（`createDatabaseIfNotExist=true`），表结构由 JPA 自动建。
- 如需修改数据库账号密码，编辑 `backend/src/main/resources/application.properties` 中的 `spring.datasource.username` / `spring.datasource.password`。

启动成功后访问 `http://localhost:8081/api/stocks` 应返回 `[]`。

> 注：默认端口 8081（8080 常被 National Instruments 等软件占用），可在 `application.properties` 中修改。

> 注：股票列表接口 `stock_basic` 在 Tushare 免费账号下限频（1 次/小时），因此列表会缓存到 `backend/stock_list_cache.tsv`（24 小时有效），限频时自动退回旧缓存，不影响搜索与采集。

## 启动前端

```bash
cd frontend
npm install
npm run dev
```

浏览器打开 `http://localhost:5173`，搜索股票代码/名称并采集，即可看到 K 线图。

## REST API

| 方法 | 路径 | 说明 |
|------|------|------|
| GET  | `/api/stocks` | 已记录股票列表 |
| GET  | `/api/stocks/search?keyword=` | 股票搜索（6 位代码 / 中文名称） |
| POST | `/api/stocks/collect` | 采集并落库，body `{code, days}`，days 默认 250 |
| GET  | `/api/stocks/{code}` | 股票详情（含最新行情） |
| GET  | `/api/stocks/{code}/daily?limit=` | 日 K 数据 |
| DELETE | `/api/stocks/{code}` | 删除股票及其数据 |
