# Spring Security Demo Application

A demo project showcasing **Spring Boot** with **Spring Security** integration. This application provides a foundation for building secure RESTful APIs with authentication and authorization support, along with Spring Boot Actuator for monitoring.

## Table of Contents

- [Prerequisites](#prerequisites)
- [Project Structure](#project-structure)
- [Dependencies](#dependencies)
- [Getting Started](#getting-started)
  - [Clone the Repository](#clone-the-repository)
  - [Build the Application](#build-the-application)
  - [Run the Application](#run-the-application)
  - [Run with Docker Compose](#run-with-docker-compose)
- [Frontend (Angular UI)](#frontend-angular-ui)
  - [UI Prerequisites](#ui-prerequisites)
  - [Install Dependencies](#install-dependencies)
  - [Run the Development Server](#run-the-development-server)
  - [Build for Production](#build-for-production)
  - [Run UI Tests](#run-ui-tests)
  - [UI Structure](#ui-structure)
- [Configuration](#configuration)
- [API Endpoints](#api-endpoints)
  - [Users](#users)
  - [Products](#products)
  - [Orders](#orders)
  - [Payments](#payments)
- [Actuator Endpoints](#actuator-endpoints)
- [Running Tests](#running-tests)

## Prerequisites

Ensure the following tools are installed before running the application:

| Tool | Minimum Version | Notes |
|------|----------------|-------|
| **Java (JDK)** | 17 | Required to compile and run the application |
| **Maven** | 3.6+ | Or use the included Maven wrapper (`mvnw`) — no separate install needed |
| **Docker & Docker Compose** | Latest stable | Required only when running via Docker Compose |
| **PostgreSQL** | 15 | Required when running locally without Docker |

> **Note:** You do not need to install Maven separately. The Maven Wrapper (`mvnw` / `mvnw.cmd`) bundled in this project will download the correct Maven version automatically.

## Project Structure

```
securitySpring/
├── frontend/                                   # Angular UI (see Frontend section)
│   ├── src/
│   │   ├── app/
│   │   │   ├── components/
│   │   │   │   ├── login/                      # Login page component
│   │   │   │   ├── home/                       # Dashboard component
│   │   │   │   └── navbar/                     # Top navigation component
│   │   │   ├── services/
│   │   │   │   └── auth.service.ts             # JWT login/logout service
│   │   │   ├── guards/
│   │   │   │   └── auth.guard.ts               # Route guard (requires login)
│   │   │   ├── interceptors/
│   │   │   │   └── auth.interceptor.ts         # Attaches JWT to HTTP requests
│   │   │   ├── app-routing.module.ts           # Client-side routes
│   │   │   └── app.module.ts                   # Root Angular module
│   │   ├── index.html
│   │   └── styles.css
│   ├── angular.json
│   └── package.json
├── src/
│   ├── main/
│   │   ├── java/com/manish/spring/security/
│   │   │   ├── SecurityApplication.java        # Application entry point
│   │   │   ├── Controller/
│   │   │   │   ├── UserController.java         # User CRUD endpoints
│   │   │   │   ├── ProductController.java      # Product CRUD endpoints
│   │   │   │   ├── OrderController.java        # Order management endpoints
│   │   │   │   └── PaymentController.java      # Payment endpoints
│   │   │   ├── Entity/
│   │   │   │   ├── User.java
│   │   │   │   ├── Role.java
│   │   │   │   ├── Product.java
│   │   │   │   ├── Order.java
│   │   │   │   ├── OrderItem.java
│   │   │   │   └── Payment.java
│   │   │   ├── Repository/
│   │   │   │   ├── UserRepository.java
│   │   │   │   ├── ProductRepository.java
│   │   │   │   ├── OrderRepository.java
│   │   │   │   └── PaymentRepository.java
│   │   │   ├── service/
│   │   │   │   ├── UserService.java
│   │   │   │   ├── ProductService.java
│   │   │   │   ├── OrderService.java
│   │   │   │   └── PaymentService.java
│   │   │   └── exception/
│   │   │       └── ResourceNotFoundException.java
│   │   └── resources/
│   │       ├── application.properties          # Application configuration
│   │       ├── docker-compose.yml              # Docker Compose setup
│   │       └── db/migration/                   # Flyway SQL migrations
│   └── test/
│       └── java/com/manish/spring/security/
│           └── SecurityApplicationTests.java
├── Dockerfile
├── pom.xml                                     # Maven project descriptor
├── mvnw / mvnw.cmd                             # Maven wrapper scripts
└── README.md
```

## Dependencies

| Dependency | Purpose |
|-----------|---------|
| `spring-boot-starter-web` | Build RESTful web APIs |
| `spring-boot-starter-data-jpa` | JPA/Hibernate ORM for database access |
| `spring-boot-starter-actuator` | Production-ready monitoring endpoints |
| `spring-boot-devtools` | Developer tools with automatic restart (runtime only) |
| `lombok` | Reduces boilerplate code via annotations |
| `postgresql` | PostgreSQL JDBC driver |
| `flyway-core` | Database schema migrations |
| `flyway-database-postgresql` | Flyway PostgreSQL dialect support |
| `spring-boot-starter-test` | Testing support for Spring Boot |

## Getting Started

### Clone the Repository

```bash
git clone https://github.com/crussaders/securitySpring.git
cd securitySpring
```

### Build the Application

**Linux / macOS:**
```bash
./mvnw clean package
```

**Windows:**
```cmd
mvnw.cmd clean package
```

This compiles the source code, runs tests, and packages the application into a JAR file under `target/`.

### Run the Application

Make sure a PostgreSQL instance is running and reachable, then start the app.

**Option 1 — Using the Maven wrapper (recommended for development):**

Linux / macOS:
```bash
./mvnw spring-boot:run
```

Windows:
```cmd
mvnw.cmd spring-boot:run
```

**Option 2 — Run the packaged JAR directly:**

```bash
java -jar target/security-0.0.1-SNAPSHOT.jar
```

Once started, the application will be available at:
```
http://localhost:8080
```

### Run with Docker Compose

The Docker Compose file at `src/main/resources/docker-compose.yml` starts four services together: PostgreSQL, Flyway (runs migrations automatically), the Spring Boot application, and pgAdmin.

```bash
cd src/main/resources
docker compose up --build
```

| Service | URL | Description |
|---------|-----|-------------|
| Spring App | `http://localhost:8080` | REST API |
| pgAdmin | `http://localhost:5050` | PostgreSQL admin UI (login: `admin@example.com` / `admin`) |

You can override the default credentials via environment variables or a `.env` file placed alongside `docker-compose.yml`:

```env
POSTGRES_DB=mydb
POSTGRES_USER=admin
POSTGRES_PASSWORD=admin123
PGADMIN_DEFAULT_EMAIL=admin@example.com
PGADMIN_DEFAULT_PASSWORD=admin
```

To stop all services:
```bash
docker compose down
```

## Frontend (Angular UI)

The `frontend/` directory contains an **Angular 19** single-page application that serves as the UI for this project. It communicates with the Spring Boot REST API running at `http://localhost:8080`.

### UI Prerequisites

| Tool | Minimum Version |
|------|----------------|
| **Node.js** | 18+ |
| **npm** | 9+ |

### Install Dependencies

```bash
cd frontend
npm install
```

### Run the Development Server

```bash
cd frontend
npm start
```

The app will be available at **`http://localhost:4200`** and will automatically reload on code changes.

> Make sure the Spring Boot backend is running at `http://localhost:8080` before using the UI.

### Build for Production

```bash
cd frontend
npm run build
```

The optimised output is written to `frontend/dist/frontend/`.

### Run UI Tests

```bash
cd frontend
npm test
```

Tests run via [Karma](https://karma-runner.github.io) in a headless Chrome browser.

### UI Structure

| Route | Component | Access |
|-------|-----------|--------|
| `/login` | `LoginComponent` | Public |
| `/home` | `HomeComponent` | Protected (requires login) |

**Key files:**

| File | Description |
|------|-------------|
| `src/app/components/login/` | Login form — calls `/auth/login`, stores JWT in `localStorage` |
| `src/app/components/home/` | Dashboard with feature cards (Users, Products, Orders, Payments) |
| `src/app/components/navbar/` | Top navigation bar with logout button |
| `src/app/services/auth.service.ts` | Handles login, logout and JWT token storage |
| `src/app/guards/auth.guard.ts` | Redirects unauthenticated users to `/login` |
| `src/app/interceptors/auth.interceptor.ts` | Attaches `Authorization: Bearer <token>` to every HTTP request |

## Configuration

Application settings are in `src/main/resources/application.properties`. Database connection details are read from environment variables, with defaults for local development:

| Property | Environment Variable | Default | Description |
|---------|---------------------|---------|-------------|
| `spring.application.name` | — | `security` | Application name |
| `spring.datasource.url` | `DB_URL` | `jdbc:postgresql://localhost:5432/mydb` | JDBC connection URL |
| `spring.datasource.username` | `DB_USERNAME` | `admin` | Database username |
| `spring.datasource.password` | `DB_PASSWORD` | `admin123` | Database password |
| `spring.jpa.hibernate.ddl-auto` | — | `update` | Hibernate DDL strategy |
| `management.endpoints.web.exposure.include` | — | `*` | Exposes all Actuator endpoints over HTTP |

To override a property at startup, pass it as a command-line argument:
```bash
./mvnw spring-boot:run -Dspring-boot.run.arguments="--server.port=9090"
```

Or supply environment variables when running the JAR:
```bash
DB_URL=jdbc:postgresql://myhost:5432/mydb DB_USERNAME=myuser DB_PASSWORD=secret \
  java -jar target/security-0.0.1-SNAPSHOT.jar
```

## API Endpoints

The base URL for all endpoints is `http://localhost:8080`. All request and response bodies use JSON.

### Users

#### List all users
```bash
curl -X GET http://localhost:8080/users
```

**Response:**
```json
[
  {
    "id": 1,
    "firstName": "Manish",
    "lastName": "Kumar",
    "email": "manish@example.com",
    "role": { "id": 1, "roleName": "ADMIN" }
  }
]
```

#### Get a user by ID
```bash
curl -X GET http://localhost:8080/users/1
```

**Response:**
```json
{
  "id": 1,
  "firstName": "Manish",
  "lastName": "Kumar",
  "email": "manish@example.com",
  "role": { "id": 1, "roleName": "ADMIN" }
}
```

#### Create a user
```bash
curl -X POST http://localhost:8080/users \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Jane",
    "lastName": "Doe",
    "email": "jane@example.com",
    "password": "secret",
    "role": { "id": 2 }
  }'
```

**Response:**
```json
{
  "id": 6,
  "firstName": "Jane",
  "lastName": "Doe",
  "email": "jane@example.com",
  "role": { "id": 2, "roleName": "CUSTOMER" }
}
```

---

### Products

#### List all products
```bash
curl -X GET http://localhost:8080/products
```

**Response:**
```json
[
  {
    "id": 1,
    "name": "Laptop",
    "description": "High performance laptop",
    "price": 999.99,
    "stock": 50,
    "createdAt": "2024-01-01T10:00:00"
  }
]
```

#### Get a product by ID
```bash
curl -X GET http://localhost:8080/products/1
```

**Response:**
```json
{
  "id": 1,
  "name": "Laptop",
  "description": "High performance laptop",
  "price": 999.99,
  "stock": 50,
  "createdAt": "2024-01-01T10:00:00"
}
```

#### Create a product
```bash
curl -X POST http://localhost:8080/products \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Wireless Mouse",
    "description": "Ergonomic wireless mouse",
    "price": 29.99,
    "stock": 200
  }'
```

**Response:**
```json
{
  "id": 6,
  "name": "Wireless Mouse",
  "description": "Ergonomic wireless mouse",
  "price": 29.99,
  "stock": 200,
  "createdAt": null
}
```

#### Update a product
```bash
curl -X PUT http://localhost:8080/products/6 \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Wireless Mouse",
    "description": "Ergonomic wireless mouse — updated",
    "price": 24.99,
    "stock": 180
  }'
```

**Response:**
```json
{
  "id": 6,
  "name": "Wireless Mouse",
  "description": "Ergonomic wireless mouse — updated",
  "price": 24.99,
  "stock": 180,
  "createdAt": null
}
```

#### Delete a product
```bash
curl -X DELETE http://localhost:8080/products/6
```

Returns HTTP `200` with an empty body.

---

### Orders

#### List all orders
```bash
curl -X GET http://localhost:8080/orders
```

**Response:**
```json
[
  {
    "id": 1,
    "user": { "id": 1, "firstName": "Manish", "lastName": "Kumar", "email": "manish@example.com" },
    "totalAmount": 1199.98,
    "status": "PENDING",
    "createdAt": "2024-01-10T12:00:00"
  }
]
```

#### Get an order by ID
```bash
curl -X GET http://localhost:8080/orders/1
```

**Response:**
```json
{
  "id": 1,
  "user": { "id": 1, "firstName": "Manish", "lastName": "Kumar", "email": "manish@example.com" },
  "totalAmount": 1199.98,
  "status": "PENDING",
  "createdAt": "2024-01-10T12:00:00"
}
```

#### Create an order
```bash
curl -X POST http://localhost:8080/orders \
  -H "Content-Type: application/json" \
  -d '{
    "user": { "id": 1 },
    "totalAmount": 59.98,
    "status": "PENDING"
  }'
```

**Response:**
```json
{
  "id": 6,
  "user": { "id": 1, "firstName": "Manish", "lastName": "Kumar", "email": "manish@example.com" },
  "totalAmount": 59.98,
  "status": "PENDING",
  "createdAt": null
}
```

#### Update order status
```bash
curl -X PUT "http://localhost:8080/orders/1/status?status=SHIPPED"
```

**Response:**
```json
{
  "id": 1,
  "user": { "id": 1, "firstName": "Manish", "lastName": "Kumar", "email": "manish@example.com" },
  "totalAmount": 1199.98,
  "status": "SHIPPED",
  "createdAt": "2024-01-10T12:00:00"
}
```

---

### Payments

#### List all payments
```bash
curl -X GET http://localhost:8080/payments
```

**Response:**
```json
[
  {
    "id": 1,
    "order": { "id": 1 },
    "paymentMethod": "CREDIT_CARD",
    "paymentStatus": "SUCCESS",
    "amount": 1199.98,
    "paymentDate": "2024-01-10T13:00:00"
  }
]
```

#### Create a payment
```bash
curl -X POST http://localhost:8080/payments \
  -H "Content-Type: application/json" \
  -d '{
    "order": { "id": 6 },
    "paymentMethod": "UPI",
    "paymentStatus": "SUCCESS",
    "amount": 59.98,
    "paymentDate": "2024-01-15T10:30:00"
  }'
```

**Response:**
```json
{
  "id": 6,
  "order": { "id": 6 },
  "paymentMethod": "UPI",
  "paymentStatus": "SUCCESS",
  "amount": 59.98,
  "paymentDate": "2024-01-15T10:30:00"
}
```

---

### Endpoint Summary

| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/users` | List all users |
| `GET` | `/users/{id}` | Get user by ID |
| `POST` | `/users` | Create a new user |
| `GET` | `/products` | List all products |
| `GET` | `/products/{id}` | Get product by ID |
| `POST` | `/products` | Create a new product |
| `PUT` | `/products/{id}` | Update a product |
| `DELETE` | `/products/{id}` | Delete a product |
| `GET` | `/orders` | List all orders |
| `GET` | `/orders/{id}` | Get order by ID |
| `POST` | `/orders` | Create a new order |
| `PUT` | `/orders/{id}/status?status=` | Update order status |
| `GET` | `/payments` | List all payments |
| `POST` | `/payments` | Create a payment |

## Actuator Endpoints

All Spring Boot Actuator endpoints are enabled. Access them at `http://localhost:8080/actuator/`:

| Endpoint | URL | Description |
|---------|-----|-------------|
| Health | `http://localhost:8080/actuator/health` | Application health status |
| Info | `http://localhost:8080/actuator/info` | Application information |
| Metrics | `http://localhost:8080/actuator/metrics` | JVM and application metrics |
| Environment | `http://localhost:8080/actuator/env` | Current environment properties |
| Beans | `http://localhost:8080/actuator/beans` | All registered Spring beans |

## Running Tests

Run the test suite using:

**Linux / macOS:**
```bash
./mvnw test
```

**Windows:**
```cmd
mvnw.cmd test
```
