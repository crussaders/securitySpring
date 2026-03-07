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
- [Configuration](#configuration)
- [Actuator Endpoints](#actuator-endpoints)
- [Running Tests](#running-tests)

## Prerequisites

Ensure the following tools are installed before running the application:

| Tool | Minimum Version | Notes |
|------|----------------|-------|
| **Java (JDK)** | 17 | Required to compile and run the application |
| **Maven** | 3.6+ | Or use the included Maven wrapper (`mvnw`) — no separate install needed |

> **Note:** You do not need to install Maven separately. The Maven Wrapper (`mvnw` / `mvnw.cmd`) bundled in this project will download the correct Maven version automatically.

## Project Structure

```
securitySpring/
├── src/
│   ├── main/
│   │   ├── java/com/manish/spring/security/
│   │   │   └── SecurityApplication.java   # Application entry point
│   │   └── resources/
│   │       └── application.properties     # Application configuration
│   └── test/
│       └── java/com/manish/spring/security/
│           └── SecurityApplicationTests.java
├── pom.xml                                # Maven project descriptor
├── mvnw / mvnw.cmd                        # Maven wrapper scripts
└── README.md
```

## Dependencies

| Dependency | Purpose |
|-----------|---------|
| `spring-boot-starter-web` | Build RESTful web APIs |
| `spring-boot-starter-actuator` | Production-ready monitoring endpoints |
| `spring-boot-devtools` | Developer tools with automatic restart (runtime only) |
| `lombok` | Reduces boilerplate code via annotations |
| `spring-boot-starter-webmvc-test` | Testing support for Spring MVC |

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

## Configuration

Application settings are located in `src/main/resources/application.properties`:

| Property | Default Value | Description |
|---------|--------------|-------------|
| `spring.application.name` | `security` | Application name |
| `management.endpoints.web.exposure.include` | `*` | Exposes all Actuator endpoints over HTTP |

To override any property at startup, pass it as a command-line argument:
```bash
./mvnw spring-boot:run -Dspring-boot.run.arguments="--server.port=9090"
```

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
