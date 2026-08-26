<div align="center">

# 🏗️ Microservices Architecture — SystemCapivara

**Technical Documentation of Distributed Infrastructure, Data Flow, and Container Orchestration**

![Architecture](https://img.shields.io/badge/Architecture-Microservices-blue?style=for-the-badge&logo=diagramsdotnet)
![Discovery](https://img.shields.io/badge/Service_Discovery-Netflix_Eureka-green?style=for-the-badge&logo=spring)
![Gateway](https://img.shields.io/badge/API_Gateway-Spring_Cloud_Gateway-green?style=for-the-badge&logo=spring)
![Resilience](https://img.shields.io/badge/Resilience-Resilience4j-red?style=for-the-badge)
![GraphQL](https://img.shields.io/badge/API-GraphQL-e10098?style=for-the-badge&logo=graphql)
![Spring AI](https://img.shields.io/badge/AI_Engine-Spring_AI-6DB33F?style=for-the-badge&logo=spring)

</div>

---

## 📌 Table of Contents
- [Architecture Overview](#-architecture-overview)
- [System Data Flow Diagram](#-system-data-flow-diagram)
- [Microservice Breakdown](#-microservice-breakdown)
- [Technology Matrix by Service](#-technology-matrix-by-service)
- [Resilience & Fault Tolerance](#-resilience--fault-tolerance)
- [Environment Configuration & `.env`](#-environment-configuration--env)
- [Compilation & Orchestration Guide](#-compilation--orchestration-guide)
- [Observability & Health Checks](#-observability--health-checks)

---

## 💡 Architecture Overview

The `servers/` directory contains the complete implementation of the distributed microservices that form the **SystemCapivara** platform. The architecture adheres to **Cloud-Native** principles, prioritizing decoupling, high availability, elasticity, and fault tolerance.

### Key Architectural Pillars
1. **Decoupled External Configuration**: Centralized property management via a remote Git repository (`app-config-repo`), fetched dynamically over SSH/HTTPS.
2. **Dynamic Service Registration & Discovery (HA)**: Eureka cluster with automatic failover between two active instances (`eureka-1` and `eureka-2`).
3. **Single Point of Entry (Reactive API Gateway)**: Intelligent routing built on *WebFlux*, *Rate Limiting* backed by Redis, and client-side load balancing via *Spring Cloud LoadBalancer*.
4. **Hybrid Communication (REST + GraphQL)**: External REST endpoints for clients, combined with internal GraphQL queries optimized for inter-service communication and aggregation.
5. **Advanced AI Integration**: Powered by Spring AI 1.1.8, offering LLM connectivity (Groq/OpenAI), MCP Client (Model Context Protocol), Vector Store for RAG, and document processing via Apache Tika.
6. **Enterprise Resilience with Circuit Breaker**: Inter-service fault isolation using Resilience4j.

---

## 📐 System Data Flow Diagram

```text
+-----------------------------------------------------------------------------------+
|                                  EXTERNAL CLIENTS                                 |
+-----------------------------------------------------------------------------------+
                                          |
                                   [ HTTP / REST ]
                                          v
+-----------------------------------------------------------------------------------+
|                            GATEWAY (Port 8080)                                    |
|   - Spring Cloud Gateway (Reactive WebFlux)                                       |
|   - Resilience4j Rate Limiter + Redis (Port 6379)                                 |
|   - LoadBalancer Client (Eureka Resolution)                                       |
+-----------------------------------------------------------------------------------+
       |                                   |                                   |
       | [Routes /core]                    | [Routes /ai]                      | [Routes /func]
       v                                   v                                   v
+-----------------------+   +-----------------------+   +-----------------------+
|  capiva-core (8081/3) |   |   capiva-ai (8088/9)  |   | capiva-scalator (8085)|
|  - Core Domain        |   |  - Spring AI 1.1.8    |   | - Spring Cloud Funct. |
|  - JPA + Flyway       |   |  - GraphQL Server     |   | - GitHub API SDK      |
|  - WebFlux GraphQL    |---|  - RAG / VectorStore  |   | - Async Processing    |
|    Client (R4j)       |   |  - MCP Client         |   +-----------------------+
+-----------------------+   +-----------------------+               |
            |                           |                           v
            v                           v                    [ GitHub API ]
    [ PostgreSQL DB ]          [ Groq / OpenAI LLM ]
```

---

## 🔬 Microservice Breakdown

### 1. `server-config` (Spring Cloud Config Server)
- **Port**: `8888`
- **Purpose**: Centralized configuration server exposing microservice property files directly from the external Git repository (`app-config-repo`).
- **Features**:
  - Utilizes SSH private key authentication via the `CAPIVA_SSH_KEY` environment variable.
  - Supports runtime property refreshes using `@RefreshScope`.

### 2. `eureka` (Netflix Eureka Server)
- **Ports**: `8761` (Instance 1) and `8762` (Instance 2)
- **Purpose**: Service registry and dynamic discovery (*Service Registry*).
- **Features**:
  - Configured in High Availability (HA) mode with cross-instance peer replication.
  - Dynamic heartbeat monitoring and automatic eviction of unresponsive instances.

### 3. `gateway` (Spring Cloud Gateway)
- **Port**: `8080`
- **Purpose**: Unified single point of entry (*Single Point of Entry*) for all external incoming HTTP traffic.
- **Features**:
  - Built on Spring WebFlux (reactive/non-blocking stack).
  - Dynamic IP/token-based Rate Limiter stored in Redis (`capivara-redis:6379`).
  - Dynamic URI resolution using Eureka service IDs (`lb://capiva-core`, `lb://capiva-ai`, etc.).

### 4. `capiva-core` (Main Core Domain Service)
- **Ports**: `8081` (Instance 1) and `8083` (Instance 2)
- **Purpose**: Encapsulates primary business logic, relational persistence, and service orchestration.
- **Features**:
  - Spring Data JPA with PostgreSQL integration (`URL_DB`, `USERNAME_DB`, `PASSWORD_DB`).
  - Database schema management and migrations powered by Flyway.
  - Consumes `capiva-ai` via GraphQL using `HttpGraphQlClient` (WebFlux).
  - Fault tolerance with Resilience4j (Circuit Breaker, Retry, Fallback).

### 5. `capiva-ai` (Artificial Intelligence Engine)
- **Ports**: `8088` (Instance 1) and `8089` (Instance 2)
- **Purpose**: Specialized engine for natural language processing, generative AI, and content synthesis.
- **Features**:
  - Spring AI 1.1.8 integrated with Groq Cloud API (`AI_API_KEY_GROQ`) and OpenAI (`AI_API_KEY_OPENAI`).
  - Vector Store support for semantic search (RAG) and Apache Tika document ingestion.
  - MCP Client (Model Context Protocol) for contextual tool execution.
  - Exposes GraphQL API endpoints for flexible consumption by internal services.

### 6. `capiva-scalator` (Serverless Integrator Service)
- **Port**: `8085`
- **Purpose**: Task-oriented function execution and serverless automation.
- **Features**:
  - Built using `Spring Cloud Function Web`.
  - Integrates with the official `github-api` SDK using `GITHUB_TOKEN`.

---

## 🧰 Technology Matrix by Service

```text
+------------------+----------+--------------+------------------+-----------------------+
| Module           | Java Ver | Spring Boot  | Spring Cloud     | Key Features          |
+------------------+----------+--------------+------------------+-----------------------+
| server-config    | JDK 21   | 4.1.0        | Config 5.0.4     | JGit, SSH Key Pem     |
| eureka           | JDK 21   | 4.1.0        | Netflix 5.0.2    | Eureka Server Cluster |
| gateway          | JDK 21   | 4.1.0        | Gateway 5.0.2    | WebFlux, Redis, R4j   |
| capiva-core      | JDK 21   | 3.5.15       | 2025.0.3         | JPA, Flyway, GraphQL  |
| capiva-ai        | JDK 21   | 3.5.15       | 2025.0.3         | Spring AI, MCP, Groq  |
| capiva-scalator  | JDK 21   | 3.5.16       | Function 4.3.4   | GitHub API SDK        |
+------------------+----------+--------------+------------------+-----------------------+
```

---

## 🛡️ Resilience & Fault Tolerance

System resilience is guaranteed through multi-layered strategies:

1. **Circuit Breaker (Resilience4j in `capiva-core`)**:
   - Monitors GraphQL invocations to `capiva-ai`. If failure rates exceed defined thresholds, the circuit opens, routing calls to a *Fallback* handler without overloading downstream services.
2. **Reactive Rate Limiting (Gateway + Redis)**:
   - Throttles requests per IP/Client using a Token Bucket algorithm stored in Redis.
3. **Proactive Retry & Timeouts**:
   - Configured automatic retry loops for transient network errors during inter-service calls.
4. **Self-Healing Health Checks**:
   - `compose.yaml` validates container health via HTTP Actuator endpoints prior to allowing dependent traffic.

---

## 🔐 Environment Configuration & `.env`

Create a `.env` file inside `servers/` based on the template below:

```env
# ============================================================
# SSH Private Key for Spring Cloud Config Server to access Git
# ============================================================
CAPIVA_SSH_KEY="-----BEGIN OPENSSH PRIVATE KEY-----\nYOUR_KEY_HERE\n-----END OPENSSH PRIVATE KEY-----"

# ============================================================
# PostgreSQL Database (capiva-core)
# ============================================================
URL_DB=jdbc:postgresql://<db-host>:5432/capivara
USERNAME_DB=postgres
PASSWORD_DB=yourpassword

# ============================================================
# AI Providers (capiva-ai)
# ============================================================
AI_API_KEY_GROQ=gsk_yourgroqkey...
AI_API_KEY_OPENAI=sk-youropenaikey...

# ============================================================
# External Integrations (capiva-scalator / capiva-ai)
# ============================================================
GITHUB_TOKEN=ghp_yourgithubtoken...
```

---

## 🚀 Compilation & Orchestration Guide

### Step 1: Build all Maven artefacts
Use the automated build script to compile and package all microservices:

```bash
chmod +x build-all.sh
./build-all.sh
```

### Step 2: Launch ecosystem using Podman Compose
Start the complete infrastructure with a single command:

```bash
podman compose up -d --build
# or with Docker:
# docker compose up -d --build
```

### Step 3: Check container status
```bash
podman compose ps
```

---

## 📈 Observability & Health Checks

All microservices expose standard monitoring endpoints:

- **Overall Health Check**
- **Prometheus Metrics**
- **Primary Eureka Dashboard**
- **Secondary Eureka Dashboard**

---

<div align="center">
<b>SystemCapivara Microservices Architecture</b> — Designed for resilience, elasticity, and intelligence.
</div>
