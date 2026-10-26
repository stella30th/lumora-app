# Lumora

Lumora is a spaced-repetition flashcard web app. Users create decks, add cards, review due cards each day, and track their progress through statistics.

Note: this project is a work in progress. Features, APIs, and documentation may change.

The repository contains two independent parts:

- `backend/`: REST API built with Spring Boot, backed by PostgreSQL.
- `frontend/`: web UI built with Next.js (App Router).

## Features

- Register, log in, and log out, with JWT access tokens and refresh tokens.
- Profile management and password change.
- Create, edit, and delete decks, including public decks.
- Create, edit, and delete cards within a deck, with pagination.
- Review queue of due cards per deck; cards are scheduled using an ease factor and an interval in days, and each card has a status of `NEW`, `LEARNING`, or `REVIEW`.
- Review statistics and daily review counts.
- Light and dark theme, dashboard, progress page, and settings page.

## Tech Stack

| Area | Technology |
| --- | --- |
| Backend | Java 17, Spring Boot 4.1, Spring Web MVC, Spring Data JPA, Spring Security, Bean Validation |
| Auth | JWT (jjwt 0.12), refresh tokens stored in the database |
| API docs | springdoc-openapi (Swagger UI) |
| Database | PostgreSQL 16 |
| Frontend | Next.js 14, React 18, TypeScript, Tailwind CSS, TanStack Query, lucide-react |
| Packaging | Maven Wrapper, Docker (multi-stage build for the backend) |

## Project Structure

```
Lumora/
  backend/
    src/main/java/com/angeltlh31/lumora/
      config/        Security, OpenAPI, and Swagger configuration
      controller/    REST controllers
      dto/           Request and response DTOs
      entity/        JPA entities
      exception/     Custom exceptions and global handler
      repository/    Spring Data repositories
      security/      JWT service and filter
      service/       Business logic
    docs/            Development notes
    docker-compose.yml
    Dockerfile
  frontend/
    src/app/         Pages (login, register, dashboard, decks, study, progress, settings)
    src/components/  UI components
    src/lib/         API client and API helpers
    src/types/       TypeScript types
    docs/            Frontend notes and spec
```

## Prerequisites

- JDK 17
- Node.js 18 or later, and npm
- Docker (for PostgreSQL)

## Getting Started

### 1. Start PostgreSQL

```bash
cd backend
docker-compose up -d
```

The `lumora` database is exposed on host port `5434`; the default user and password are both `lumora`.

### 2. Run the backend

```bash
cd backend
./mvnw spring-boot:run
```

On Windows use `mvnw.cmd spring-boot:run`. The API runs at `http://localhost:8080`. Swagger UI is available at `http://localhost:8080/swagger-ui.html` and the OpenAPI spec at `/v3/api-docs`.

### 3. Run the frontend

```bash
cd frontend
npm install
npm run dev
```

The app runs at `http://localhost:3000`.

## Configuration

### Backend

| Variable | Default | Description |
| --- | --- | --- |
| `PORT` | `8080` | Server port |
| `JWT_SECRET` | dev value in `application.properties` | JWT signing key, at least 32 characters. Must be overridden in production |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:3000` | Comma-separated list of frontend origins allowed to call the API |

Database connection settings and token lifetimes (access token 15 minutes, refresh token 7 days) live in `backend/src/main/resources/application.properties`. For production, override the database settings and do not use `spring.jpa.hibernate.ddl-auto=update`.

### Frontend

| Variable | Default | Description |
| --- | --- | --- |
| `NEXT_PUBLIC_API_URL` | `http://localhost:8080` | Backend base URL |

## API Overview

| Group | Endpoints |
| --- | --- |
| Users | `POST /api/users/register`, `POST /api/users/login`, `POST /api/users/refresh`, `POST /api/users/logout`, `GET /api/users/me`, `PUT /api/users/{id}`, `PUT /api/users/password` |
| Decks | `POST /api/decks`, `GET /api/decks`, `GET /api/decks/public`, `GET/PUT/DELETE /api/decks/{id}` |
| Cards | `POST/GET /api/decks/{deckId}/cards`, `GET/PUT/DELETE /api/cards/{id}` |
| Review | `POST /api/cards/{cardId}/review`, `GET /api/decks/{deckId}/review-queue` |
| Progress | `GET /api/progress/stats` |

See Swagger UI for the authoritative list and request/response schemas.

## Build and Deployment

The backend includes a multi-stage `Dockerfile` (JDK 17 to build, JRE 17 to run) and reads its port from `PORT`, which suits platforms such as Render or Railway.

```bash
cd backend
docker build -t lumora-backend .
```

Frontend:

```bash
cd frontend
npm run build
npm run start
```

When deploying, set `NEXT_PUBLIC_API_URL` on the frontend to the backend URL and `CORS_ALLOWED_ORIGINS` on the backend to the frontend domain.
