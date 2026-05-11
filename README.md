# Automation Testing — E-commerce App

This repository demonstrates my architectural approach to automation in testing. It wraps a full-stack e-commerce application with multiple test automation suites — each showcasing different language/framework combinations while sharing a common layered design (channel abstraction, page objects, API clients, factory-based test data).

## ⭐ Reviewing this repo? Run `/automation-overview`

If you're here to assess my automation-testing skills, don't read this whole README. Open the repo in [Claude Code](https://docs.claude.com/en/docs/claude-code/overview) and run:

```
/automation-overview
```

It's a slash command (see [`.claude/commands/automation-overview.md`](.claude/commands/automation-overview.md)) that walks you through the repo conversationally. It first asks whether you want a recruiter-friendly tour or an engineer-to-engineer deep dive, gives you the overview at that level, then lets you pick what to explore further — channels, adapters, BDD, WireMock, CI/CD, k6, or backend tests.

Faster than clicking around. No Claude Code? Keep reading — the same material is in [Automation Suites](#automation-suites) and [CI/CD](#continuous-integration--continuous-delivery-cicd).

## Automation Suites

| Suite | Stack | Channel(s) |
|-------|-------|-------------|
| **java-automation** | Serenity BDD + Cucumber + Selenium / Playwright + REST Assured / OkHttp | Web, API |
| **kotlin-automation** | Serenity BDD + Cucumber + Selenium + REST Assured | Web, API |
| **typescript-automation** | CucumberJS + Playwright + Axios | Web, API |
| **frontend/e2e** | Playwright (direct) | Web |
| **performance/k6** | k6 (smoke + load) | API |

Each suite is runnable independently and exercises the same application through both the UI (Web channel) and HTTP endpoints (API channel), validating that the same business behavior holds regardless of entry point.

### Architecture Highlights

- **Channel abstraction** — step definitions delegate to a channel interface (Web or API), so the same Cucumber scenarios run against both without duplication
- **Browser adapter pattern** (Java/Kotlin) — Selenium and Playwright are interchangeable behind a common browser interface
- **HTTP adapter pattern** (Java) — REST Assured and OkHttp are interchangeable behind a common HTTP client interface
- **WireMock isolation** — frontend tests can run against a stubbed backend for fast, deterministic feedback
- **Performance baseline** — k6 smoke and load tests gate the CI pipeline

## Continuous Integration & Continuous Delivery (CI/CD)

The pipeline runs on **GitHub Actions** (GHA) — see [`.github/workflows/ci.yml`](.github/workflows/ci.yml). Continuous Integration (CI) fires on every push and pull request (PR). Continuous Delivery (CD) publishes Docker images to the GitHub Container Registry (GHCR), but only after `main` passes every gate below.

### Pipeline stages and quality gates

Jobs run as a directed acyclic graph (DAG): each stage is a quality gate (QG) and downstream stages don't start until the upstream ones pass.

| # | Stage / Quality Gate (QG) | Tooling | Purpose |
|---|---------------------------|---------|---------|
| 1 | **Static analysis** | Ruff (lint + format), mypy (types), ESLint, Prettier, `tsc --noEmit` | Catch style, lint, and type errors before any test runs |
| 2 | **Frontend build** | TypeScript Compiler (TSC) + Vite | Produce a production bundle; emit a build artifact reused by every downstream UI suite |
| 3 | **Backend unit tests** | pytest + coverage.py | Fast unit and integration tests against an in-memory SQLite database |
| 4 | **Playwright end-to-end (E2E)** | Playwright (TS) | Browser E2E tests against a real backend |
| 5 | **Java acceptance — Web channel** | Serenity BDD + Cucumber + Selenium | BDD acceptance tests through the UI |
| 6 | **Java acceptance — API channel** | Serenity BDD + Cucumber + REST Assured | Same Cucumber scenarios, API channel |
| 7 | **Java acceptance — Web channel (Playwright adapter)** | Serenity + Cucumber + Playwright (Java) | Demonstrates the browser adapter pattern: same scenarios, different driver |
| 8 | **Java acceptance — API channel (OkHttp adapter)** | Serenity + Cucumber + OkHttp | Demonstrates the HTTP client adapter pattern |
| 9 | **Kotlin acceptance — Web channel** | Serenity BDD + Cucumber + Selenium | Same architecture, different JVM language |
| 10 | **Kotlin acceptance — API channel** | Serenity BDD + Cucumber + REST Assured | Cross-language consistency check |
| 11 | **TypeScript acceptance — Web channel** | CucumberJS + Playwright | BDD with a JS/TS toolchain |
| 12 | **TypeScript acceptance — API channel** | CucumberJS + Axios | API-only BDD via Axios |
| 13 | **Playwright E2E — WireMock isolation** | Playwright + WireMock (WM) | Frontend E2E against a stubbed backend; gives fast, deterministic feedback even when the backend is broken or not yet built |
| 14 | **Java Web acceptance — WireMock isolation** | Serenity + Cucumber + WireMock | Same isolation strategy, JVM stack |
| 15 | **Security scanning** | pip-audit, npm audit, OWASP Dependency-Check | Software Composition Analysis (SCA) — flags known Common Vulnerabilities and Exposures (CVEs) in third-party packages |
| 16 | **Publish (CD)** | Docker Buildx → GHCR | Build and push immutable container images for backend and frontend; only on `main`, only after all gates pass |
| 17 | **Performance — k6** | k6 (smoke + load) | Performance regression baseline against the running backend |

A weekly cron (Mondays, 06:00 UTC) re-runs the pipeline so the security and performance baselines stay current between PRs.

### How the gates map to the Testing Pyramid

The Testing Pyramid says: lots of fast unit tests at the bottom, fewer integration tests in the middle, a handful of slow end-to-end (E2E) tests at the top. That's the shape this pipeline implements — plus two cross-cutting gates (security and performance) that every release has to clear.

```
                          ╱────╲
                         ╱  E2E ╲             ← QG 4, 5, 7, 9, 11, 13, 14
                        ╱  (UI)  ╲              Playwright + Serenity/Cucumber Web channels
                       ╱──────────╲             (incl. WireMock isolation variants)
                      ╱ Acceptance ╲          ← QG 6, 8, 10, 12
                     ╱  (API/BDD)   ╲           Cucumber scenarios over HTTP — same specs as UI,
                    ╱────────────────╲          but via REST Assured / OkHttp / Axios
                   ╱   Integration    ╲       ← QG 3 (subset)
                  ╱  (FastAPI + DB)    ╲        pytest hitting real routes against in-memory SQLite
                 ╱──────────────────────╲
                ╱       Unit tests       ╲    ← QG 3 (subset)
               ╱   (pure functions, CRUD) ╲     pytest unit-level coverage of app/crud, app/schemas, etc.
              ╱──────────────────────────╲
             ╱      Static analysis        ╲  ← QG 1
            ╱  (lint + types + format)      ╲   Ruff, mypy, ESLint, Prettier, TSC
           ╱──────────────────────────────────╲

   Cross-cutting gates (run against the whole stack, not a single layer):
     • Security scanning (SCA / CVE)  ← QG 15  pip-audit, npm audit, OWASP Dependency-Check
     • Performance baseline (k6)      ← QG 17  smoke + load
     • Continuous Delivery (CD)       ← QG 16  Docker → GHCR, only after every gate above is green
```

A few choices worth calling out:

- **The acceptance layer is wider than usual, on purpose.** The same Cucumber `.feature` files drive both the UI and the API, so each scenario gets exercised twice — once as a realistic browser flow, once as a fast HTTP contract — with no duplication. That's what the channel abstraction buys you.
- **Three languages, one shape.** Java, Kotlin, and TypeScript all live at the acceptance layer. They aren't redundant — they prove the architecture (channels, browser/HTTP adapters, BDD) ports cleanly across ecosystems.
- **WireMock isolation isn't a footnote.** The Playwright and Java Web suites both have a WireMock (WM) variant that runs against a stub. They catch frontend regressions even when the backend is broken or hasn't been built yet — something the classical pyramid doesn't model, but every real pipeline needs.
- **Security and performance are gates, not "nice to haves".** SCA (QG 15) and k6 (QG 17) run on every push to `main` and on the weekly cron. They sit outside the pyramid because they assert properties about the whole system, not a single layer.
- **CD falls out of CI.** There's no separate release workflow. When every gate is green on `main`, the publish job (QG 16) ships the images. Every green merge is a release candidate.

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
