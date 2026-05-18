# python-automation

Python BDD acceptance tests for the Linea Supply e-commerce demo. Mirrors the architecture of `java-automation/`, `kotlin-automation/`, and `typescript-automation/`.

## Stack

- **BDD framework**: [behave](https://behave.readthedocs.io/) (standard Gherkin parser, full Cucumber-JVM parity)
- **Web adapters**: Playwright (default) and Selenium — swap via `BROWSER_IMPL` env var
- **HTTP adapters**: httpx (default) and requests — swap via `HTTP_IMPL` env var
- **Channel selection**: `CHANNEL=Web` or `CHANNEL=API`

## Run

From repo root:

```bash
just install-py                       # uv sync + playwright install chromium
just acceptance-py-web                # Playwright web channel (27 scenarios)
just acceptance-py-web-selenium       # Selenium web channel (27 scenarios)
just acceptance-py-api                # httpx api channel (8 scenarios)
just acceptance-py-api-requests       # requests api channel (8 scenarios)
```

## Layout

```
features/                 # Gherkin feature files + behave steps + environment.py
src/automation/
  dsl/
    protocols/            # CatalogueProtocol, CartProtocol, SavedProtocol (ABCs)
    domain/               # Product, CartState, SavedState dataclasses
  driver/
    ports/                # BrowserPort, HttpPort (ABCs)
    web/                  # Playwright + Selenium adapters
    api/                  # httpx + requests adapters
```
