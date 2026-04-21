# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**Linea Supply** — a full-stack e-commerce demo (FastAPI backend + React/TypeScript frontend) designed specifically to showcase TDD and automation testing practices.

- **Backend:** FastAPI + SQLModel + SQLite (port 8001) — `backend/`
- **Frontend:** React 18 + TypeScript + Vite + Chakra UI (port 3001) — `frontend/`
- **E2E Tests:** Playwright — `frontend/e2e/tests/`
- **Backend Tests:** pytest — `backend/tests/`

## Commands

### Setup
```bash
just install-all      # Install Python deps (uv), Node deps, Playwright browsers
```

### Development
```bash
just dev              # Start both services with hot-reload (interactive)
just dev-headless     # Start both services in background (use for agentic/automated workflows)
just stop             # Stop background services
just logs             # View last 100 lines from both service logs
just seed             # Populate SQLite database with sample data
```

### Testing
```bash
just test-local                    # Run backend pytest suite
just test-local-single TEST        # Run single backend test, e.g. tests/api/test_products.py::test_create
just test-e2e                      # Run Playwright E2E tests (headless)
just test-e2e-headed               # Run E2E tests with visible browser (debugging)
just test-all-local                # Run backend + E2E tests sequentially
```

### Code Quality
```bash
just check            # Ruff lint + mypy type-check (backend)
just format           # Ruff format (backend) + Prettier (frontend)
just lint             # ESLint (frontend)
just build            # TypeScript compile + Vite production build
just ci               # Full CI pipeline locally (format check → lint → tests → build → e2e)
```

### Database
```bash
just reset-db                       # Delete store.db
just migrate-create "description"   # Create Alembic migration
just migrate-up / just migrate-down # Run/rollback migrations
```

## Architecture

### Data Flow
```
SQLite ↔ SQLModel (ORM) ↔ Pydantic schemas ↔ FastAPI routes ↔ React Context ↔ Chakra UI components
```

### Backend Structure
- `backend/app/main.py` — all route definitions
- `backend/app/models.py` — SQLModel ORM models (Product, Category, DeliveryOption)
- `backend/app/crud.py` — all database operations
- `backend/app/schemas.py` — Pydantic request/response schemas
- Product images are stored as BLOBs directly in SQLite
- Relationships: Product ↔ Category (many-to-one), Product ↔ DeliveryOption (many-to-many)

### Frontend Structure
- State management via React Context API (`frontend/src/context/`)
- API calls abstracted in `frontend/src/api/`
- E2E tests use `data-testid` attributes via a centralized `SELECTORS` object (`frontend/e2e/tests/utils/selectors.ts`)
- Custom Playwright wait utilities in `frontend/e2e/tests/utils/waits.ts`

### Testing Architecture
- **Backend tests** use `TestClient` with a per-session in-memory SQLite DB (fixtures in `conftest.py`), and factory functions in `factories.py` for test data creation
- **E2E tests** clear `localStorage` in `beforeEach`, run against real running services (Playwright auto-starts backend via `playwright.config.ts`)

## Testing Principles

- Test **expected behavior** through the public API only — never test internals
- No 1:1 mapping between test files and implementation files
- **100% coverage** expected for all business logic, verified by behavior not implementation
- Always use factory functions with optional overrides for test data:

```python
# Python backend
def create_test_product(overrides: dict = None) -> Product:
    base = {"name": "Test Product", "price": 29.99, ...}
    return Product(**(base | (overrides or {})))
```

```typescript
// TypeScript frontend
const getMockProduct = (overrides?: Partial<Product>): Product => ({
  id: "prod_123", name: "Test Product", ...overrides,
});
```

## Code Style

**Python:**
- Type hints required on all functions; `mypy` enforces this — never use `# type: ignore`
- Use `HTTPException` for error handling, `FastAPI Depends()` for dependency injection
- Prefer options objects (`@dataclass`) over long parameter lists

**TypeScript:**
- Strict mode enforced — no `any`, no `as SomeType` casts without justification, no `@ts-ignore`
- Functional components only; Formik + Yup for forms

**Both:** No comments in code — naming should be self-documenting.

## GitHub Operations

Always use the GitHub CLI (`gh`) for all repository interactions (issues, PRs, comments). Do not use `curl` or MCP for GitHub API calls.
