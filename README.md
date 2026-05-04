# Automation Testing — E-commerce App

This repository demonstrates my architectural approach to automation in testing. It wraps a full-stack e-commerce application with multiple test automation suites — each showcasing different language/framework combinations while sharing a common layered design (channel abstraction, page objects, API clients, factory-based test data).

## Automation Suites

| Suite | Stack | Channel(s) |
|-------|-------|-------------|
| **java-automation** | Serenity BDD + Cucumber + Selenium / Playwright + REST Assured / OkHttp | Web, API |
| **kotlin-automation** | Serenity BDD + Cucumber + Selenium + REST Assured | Web, API |
| **typescript-automation** | CucumberJS + Playwright + Axios | Web, API |
| **frontend/e2e** | Playwright (direct) | Web |

Each suite is runnable independently and exercises the same application through both the UI (Web channel) and HTTP endpoints (API channel), validating that the same business behavior holds regardless of entry point.

### Architecture Highlights

- **Channel abstraction** — step definitions delegate to a channel interface (Web or API), so the same Cucumber scenarios run against both without duplication
- **Browser adapter pattern** (Java/Kotlin) — Selenium and Playwright are interchangeable behind a common browser interface
- **HTTP adapter pattern** (Java) — REST Assured and OkHttp are interchangeable behind a common HTTP client interface
- **WireMock isolation** — frontend tests can run against a stubbed backend for fast, deterministic feedback
- **Performance baseline** — k6 smoke and load tests gate the CI pipeline

## The Application Under Test

The system under test is **Linea Supply**, a self-contained e-commerce demo originally based on [sourcegraph/ecommerce-app](https://github.com/sourcegraph/ecommerce-app).

- **Backend:** FastAPI + SQLModel + SQLite (port 8001)
- **Frontend:** React 18 + TypeScript + Vite + Chakra UI (port 3001)
- **Data:** SQLite with product images stored as BLOBs; relationships between Products, Categories, and Delivery Options

### Data Flow

```
SQLite <-> SQLModel (ORM) <-> Pydantic schemas <-> FastAPI routes <-> React Context <-> Chakra UI components
```

### Project Structure

```
.
├── backend/                 # FastAPI application
│   ├── app/                 # Routes, models, CRUD, schemas
│   ├── tests/               # pytest unit/integration tests
│   └── alembic/             # Database migrations
├── frontend/                # React + Vite application
│   ├── src/                 # Components, context, API client
│   └── e2e/                 # Playwright E2E tests
├── java-automation/         # Java test automation suite
├── kotlin-automation/       # Kotlin test automation suite
├── typescript-automation/   # TypeScript test automation suite
├── performance/             # k6 smoke and load tests
├── wiremock/                # WireMock stubs for frontend isolation
└── .github/workflows/       # CI pipeline
```

## Quick Start

### Prerequisites

- Python 3.13+ (via [uv](https://github.com/astral-sh/uv))
- Node.js 24+
- Java 21+ (for java-automation / kotlin-automation)
- [just](https://github.com/casey/just) command runner

### Setup and Run

```bash
just install-all      # Install Python deps, Node deps, Playwright browsers
just seed             # Populate the database with sample data
just dev              # Start backend + frontend with hot-reload
```

Access the application:
- Frontend: http://localhost:3001
- Backend API: http://localhost:8001

### Running Tests

```bash
# Application tests
just test-local                    # Backend pytest suite
just test-e2e                      # Playwright E2E tests

# Java automation (Web + API channels)
cd java-automation && ./gradlew test -Dchannel=Web
cd java-automation && ./gradlew test -Dchannel=API -Dcucumber.filter.tags="@api and not @wip"

# Kotlin automation
cd kotlin-automation && ./gradlew test -Dchannel=Web
cd kotlin-automation && ./gradlew test -Dchannel=API -Dcucumber.filter.tags="@api and not @wip"

# TypeScript automation
cd typescript-automation && CHANNEL=Web npx cucumber-js --tags 'not @wip'
cd typescript-automation && CHANNEL=API npx cucumber-js --tags '@api and not @wip'

# Full CI pipeline locally
just ci
```

### Code Quality

```bash
just check            # Ruff lint + mypy type-check (backend)
just format           # Ruff format (backend) + Prettier (frontend)
just lint             # ESLint (frontend)
just build            # TypeScript compile + Vite production build
```

### Database Management

```bash
just reset-db                       # Delete store.db
just migrate-create "description"   # Create Alembic migration
just migrate-up / just migrate-down # Run/rollback migrations
```
