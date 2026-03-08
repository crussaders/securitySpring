# Security Spring — Angular Frontend

An **Angular 19** single-page application that provides the UI for the Security Spring backend. It communicates with the Spring Boot REST API at `http://localhost:8080`.

## Table of Contents

- [Prerequisites](#prerequisites)
- [Getting Started](#getting-started)
- [Available Scripts](#available-scripts)
- [Application Structure](#application-structure)
- [Routing](#routing)
- [Authentication Flow](#authentication-flow)
- [HTTP Interceptor](#http-interceptor)
- [Running Tests](#running-tests)
- [Building for Production](#building-for-production)
- [Code Scaffolding](#code-scaffolding)

## Prerequisites

| Tool | Minimum Version |
|------|----------------|
| **Node.js** | 18+ |
| **npm** | 9+ |
| **Angular CLI** | 19+ (optional — `npx ng` works without a global install) |

The Spring Boot backend must be running at `http://localhost:8080` before using the UI.

## Getting Started

```bash
# from the repo root
cd frontend
npm install
npm start
```

Open **`http://localhost:4200`** in your browser. The app will automatically reload when you change source files.

## Available Scripts

| Command | Description |
|---------|-------------|
| `npm start` | Start the development server at `http://localhost:4200` |
| `npm run build` | Build optimised production bundle into `dist/frontend/` |
| `npm test` | Run unit tests via Karma (headless Chrome) |
| `npm run watch` | Build in watch mode for development |

## Application Structure

```
src/
└── app/
    ├── components/
    │   ├── login/          # Login page — username/password form
    │   ├── home/           # Protected dashboard with feature cards
    │   └── navbar/         # Top navigation bar with logout
    ├── services/
    │   └── auth.service.ts # Login, logout and JWT token management
    ├── guards/
    │   └── auth.guard.ts   # Prevents unauthenticated access to protected routes
    ├── interceptors/
    │   └── auth.interceptor.ts  # Injects Authorization header on all requests
    ├── app-routing.module.ts    # Client-side route definitions
    └── app.module.ts            # Root Angular module
```

## Routing

| Path | Component | Guard |
|------|-----------|-------|
| `/` | Redirects to `/home` | — |
| `/login` | `LoginComponent` | Public |
| `/home` | `HomeComponent` | `authGuard` (login required) |
| `**` | Redirects to `/home` | — |

## Authentication Flow

1. User submits credentials on the **Login** page.
2. `AuthService.login()` posts to `POST /auth/login` on the backend.
3. On success, the JWT token is saved to `localStorage` under the key `auth_token`.
4. The user is navigated to `/home`.
5. Clicking **Logout** clears the token and redirects to `/login`.

## HTTP Interceptor

`AuthInterceptor` automatically attaches a `Authorization: Bearer <token>` header to every outgoing HTTP request when a token is present in `localStorage`. No manual header management is needed in services or components.

## Running Tests

```bash
npm test
```

Tests run via [Karma](https://karma-runner.github.io) with Jasmine in a headless Chrome browser. Test files follow the `*.spec.ts` naming convention and live alongside their source files.

## Building for Production

```bash
npm run build
```

The optimised bundle is output to `dist/frontend/`. You can serve this directory with any static file server or configure the Spring Boot app to serve it directly.

## Code Scaffolding

Use the Angular CLI to generate new building blocks:

```bash
npx ng generate component components/my-component
npx ng generate service services/my-service
npx ng generate guard guards/my-guard
```

For more information see the [Angular CLI documentation](https://angular.dev/tools/cli).
