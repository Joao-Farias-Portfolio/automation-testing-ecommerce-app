# FastAPI + React E-commerce Demo Automation
# Install 'just' via: brew install just (macOS) or cargo install just

# ─── variables ──────────────────────────────────────────────
LOG_DIR := "logs"
# Latest Node 22 from nvm (Vite requires Node 20+ / 22+)
NODE22 := `ls -d $HOME/.nvm/versions/node/v22.* 2>/dev/null | sort -V | tail -1`

# Internal helper to ensure logs directory exists
_ensure-logs-dir:
    @mkdir -p {{LOG_DIR}}

# Default recipe to display available commands
default:
    @just --list

# Install backend dependencies
install:
    cd backend && uv sync

# Install frontend dependencies
install-frontend:
    cd frontend && npm install

# Install E2E test dependencies
install-e2e:
    cd frontend && npm install --save-dev @playwright/test
    cd frontend && npx playwright install --with-deps

# Install ALL project deps (Python, Node, Playwright browsers)
install-all:
    just install
    just install-frontend
    just install-e2e

# ─── native dev workflow ────────────────────────────────────────
# Run backend + frontend concurrently (hot-reload)
dev:
    @echo "Starting native dev servers on 3001/8001 ..."
    cd frontend && npx concurrently \
      --names "backend,frontend" \
      --prefix-colors "blue,green" \
      "cd ../backend && uv run --active uvicorn app.main:app --reload --reload-exclude '.venv/*' --host 0.0.0.0 --port 8001" \
      "npm run dev -- --host 0.0.0.0 --port 3001"

# Run backend + frontend in background (detached for agentic tools)
dev-headless: _ensure-logs-dir
    @echo "Starting headless dev servers on 3001/8001 ..."
    # truncate old logs so each run starts fresh
    > {{LOG_DIR}}/backend.log
    > {{LOG_DIR}}/frontend.log
    # backend
    cd backend && uv run --active uvicorn app.main:app --reload --reload-exclude '.venv/*' --host 0.0.0.0 --port 8001 >> ../{{LOG_DIR}}/backend.log 2>&1 & echo $$! > {{LOG_DIR}}/backend.pid
    # frontend (must use Node 22+ for Vite)
    cd frontend && PATH="{{NODE22}}/bin:$PATH" npm run dev -- --host 0.0.0.0 --port 3001 >> ../{{LOG_DIR}}/frontend.log 2>&1 & echo $$! > {{LOG_DIR}}/frontend.pid
    @echo "Services started in background. Use 'just logs' to inspect and 'just stop' to stop."

# Stop headless dev servers
stop:
    @echo "Stopping headless dev servers..."
    -@test -f {{LOG_DIR}}/backend.pid && kill -TERM `cat {{LOG_DIR}}/backend.pid` 2>/dev/null || true
    -@test -f {{LOG_DIR}}/frontend.pid && kill -TERM `cat {{LOG_DIR}}/frontend.pid` 2>/dev/null || true
    @sleep 2
    -@lsof -ti:8001,3001 -sTCP:LISTEN 2>/dev/null | grep . | xargs kill -TERM 2>/dev/null || true
    @sleep 2
    -@lsof -ti:8001,3001 -sTCP:LISTEN 2>/dev/null | grep . | xargs kill -KILL 2>/dev/null || true
    @rm -f {{LOG_DIR}}/backend.pid {{LOG_DIR}}/frontend.pid
    @echo "Services stopped."

# Independent servers (sometimes handy)
dev-backend:
    cd backend && uv run --active uvicorn app.main:app --reload --reload-exclude '.venv/*' --host 0.0.0.0 --port 8001

dev-frontend:
    cd frontend && npm run dev -- --host 0.0.0.0 --port 3001

# Show the last 100 lines of each log
logs:
    @echo "─── Backend (last 100 lines) ───"
    @tail -n 100 {{LOG_DIR}}/backend.log || echo "No backend log yet."
    @echo
    @echo "─── Frontend (last 100 lines) ───"
    @tail -n 100 {{LOG_DIR}}/frontend.log || echo "No frontend log yet."

# Follow both logs live (Ctrl-C to quit)
logs-follow:
    @echo "Tailing backend & frontend logs (Ctrl+C to exit)..."
    @tail -F {{LOG_DIR}}/backend.log {{LOG_DIR}}/frontend.log



# Seed the database with products and images
seed:
    cd backend && uv run --active python -m app.seed



# ─── testing ──────────────────────
# Run backend tests locally (requires: uv sync)
test-local:
    cd backend && uv run --active pytest

# Run single test locally
test-local-single TEST:
    cd backend && uv run --active pytest {{TEST}}

# Run E2E tests natively (headless - default)
test-e2e:
    @echo "Running E2E tests natively (headless)..."
    @just stop
    @sleep 3
    cd frontend && npx playwright test

# Run E2E tests natively (headed mode for debugging)
test-e2e-headed:
    @echo "Running E2E tests natively (headed)..."
    cd frontend && HEADED=1 npx playwright test

# Setup E2E testing (install browsers)
setup-e2e:
    cd frontend && npx playwright install --with-deps chromium

# Run all tests (backend + E2E)
test-all-local:
    @just test-local
    @just test-e2e

# Run CI checks locally (mirrors CI pipeline)
ci:
    @echo "Running full CI pipeline locally..."
    cd backend && uv run --active ruff format --check .
    @just check
    @just test-local
    cd frontend && npx prettier --check .
    cd frontend && npm run lint
    @just build
    @just test-e2e
    @echo "All CI checks passed!"



# Check backend code quality
check:
    cd backend && uv run --active ruff check .
    cd backend && uv run --active mypy .

# Format backend and frontend code
format:
    cd backend && uv run --active ruff format .
    cd frontend && npm run format

lint:
    cd frontend && npm run lint

# Build frontend for production
build:
    cd frontend && npm run build

# Reset database (removes store.db files)
reset-db:
    cd backend && rm -f store.db store.db-*

# Health check for running services
health:
    @echo "Checking service health..."
    @curl -f http://localhost:8001/health || echo "Backend not responding"
    @curl -f http://localhost:3001 || echo "Frontend not responding"

# Run database shell (SQLite CLI)
db-shell:
    cd backend && sqlite3 store.db

# Database migration commands
migrate-create MESSAGE:
    cd backend && uv run --active alembic revision --autogenerate -m "{{MESSAGE}}"

migrate-up:
    cd backend && uv run --active alembic upgrade head

migrate-down:
    cd backend && uv run --active alembic downgrade -1

migrate-history:
    cd backend && uv run --active alembic history

# ─── java-automation ──────────────────────────────────────────────────────────

# Generate Gradle wrapper (requires Gradle installed: brew install gradle)
install-java:
    @which gradle > /dev/null || (echo "Install Gradle first: brew install gradle" && exit 1)
    cd java-automation && gradle wrapper --gradle-version 9.4.1
    @echo "Gradle wrapper created. Run: just test-java"

# Run all Serenity BDD Java E2E tests (headless, generates HTML report)
test-java:
    cd java-automation && ./gradlew test
    @echo "Report: java-automation/target/site/serenity/index.html"

# Run Java tests with visible browser
test-java-headed:
    cd java-automation && ./gradlew test -Denvironment=headed

# Run tests for a specific tag  (e.g.: just test-java-tag TAG=@cart)
test-java-tag TAG:
    cd java-automation && ./gradlew test -Dcucumber.filter.tags="{{TAG}}"

# Run tests for a specific feature file  (e.g.: just test-java-feature FEATURE=cart)
test-java-feature FEATURE:
    cd java-automation && ./gradlew test -Dcucumber.filter.tags="@{{FEATURE}}"

# Open the Serenity HTML report
report-java:
    @open java-automation/target/site/serenity/index.html

# ─── java-api-testing ─────────────────────────────────────────────────────────

# Run Serenity BDD REST API tests (requires backend running on :8001)
test-java-api:
    cd java-api-testing && ./gradlew test
    @echo "Report: java-api-testing/target/site/serenity/index.html"

# Run API tests for a specific tag  (e.g.: just test-java-api-tag TAG=@products)
test-java-api-tag TAG:
    cd java-api-testing && ./gradlew test -Dcucumber.filter.tags="{{TAG}}"

# Open the API test Serenity HTML report
report-java-api:
    @open java-api-testing/target/site/serenity/index.html

# Zero-to-verify: reset DB, seed, start services, run both Java test suites
test-java-all:
    @echo "=== Stopping any running services ==="
    @just stop
    @echo "=== Resetting and seeding database ==="
    @just reset-db
    @echo "=== Starting services (headless) ==="
    @just dev-headless
    @echo "=== Waiting for backend (port 8001) ==="
    @until curl -sf http://localhost:8001/health > /dev/null 2>&1; do sleep 1; done
    @echo "Backend ready."
    @echo "=== Waiting for frontend (port 3001) ==="
    @until curl -sf http://localhost:3001 > /dev/null 2>&1; do sleep 1; done
    @echo "Frontend ready."
    @echo "=== Seeding database with test data ==="
    @just seed
    @echo "=== Running Java E2E tests ==="
    @just test-java
    @echo "=== Running Java API tests ==="
    @just test-java-api
    @echo ""
    @echo "=== All Java tests complete ==="
    @echo "E2E report:  java-automation/target/site/serenity/index.html"
    @echo "API report:  java-api-testing/target/site/serenity/index.html"

# Run all test suites (backend pytest + Playwright E2E + Serenity Java + Serenity API)
test-all:
    @just test-local
    @just test-e2e
    @just test-java
    @just test-java-api
