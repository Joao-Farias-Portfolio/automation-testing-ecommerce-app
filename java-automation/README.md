# java-automation

Serenity BDD + Cucumber acceptance tests for the Linea Supply e-commerce app. The same Gherkin scenarios run against two interchangeable channels: a real browser (Selenium) or the backend HTTP API (REST Assured). No scenario knows which channel it is using.

---

## CI / CD

The Java acceptance tests run as part of the repository CI pipeline:

- **CI pipeline definition:** [`/.github/workflows/ci.yml`](https://github.com/Joao-Farias-Portfolio/automation-testing-ecommerce-app/blob/main/.github/workflows/ci.yml)
- **CI run history:** [GitHub Actions](https://github.com/Joao-Farias-Portfolio/automation-testing-ecommerce-app/actions)
- **Performance tests:** [`/performance/`](https://github.com/Joao-Farias-Portfolio/automation-testing-ecommerce-app/tree/main/performance)

---

## How to run

Prerequisites: Java 21+, Gradle wrapper (`./gradlew`), Chrome installed. For the API channel, the FastAPI backend must be on port 8001 (`just dev-headless` from the repo root).

```bash
# Web channel — 26 scenarios in a real Chrome browser
just acceptance-web

# API channel — 6 scenarios directly against the backend HTTP API
just acceptance-api

# Open the Serenity HTML report after either run
just report-java
```

You can also invoke Gradle directly and filter by tag:

```bash
./gradlew test -Dchannel=Web
./gradlew test -Dchannel=API -Dcucumber.filter.tags="@api and not @wip"
./gradlew test -Dchannel=Web -Dcucumber.filter.tags="@smoke"
```

Scenarios tagged `@wip` are excluded from all runs by default (see `build.gradle.kts` and the `@wip` section below).

---

## Package structure

```
src/test/java/com/myecommerce/automation/
│
├── runner/
│   └── CucumberTestSuite.java          Entry point — JUnit4 + Serenity runner
│
├── hooks/
│   └── Hooks.java                      @Before / @After: start/stop Chrome (Web channel only)
│
├── dsl/
│   ├── domain/                         Plain Java records — shared by all channels
│   │   ├── ProductCard.java
│   │   ├── ProductListing.java
│   │   ├── ProductDetail.java
│   │   ├── CartItem.java
│   │   ├── CartState.java
│   │   ├── DeliveryOption.java
│   │   ├── DeliveryState.java
│   │   ├── SavedState.java
│   │   └── SearchResults.java
│   │
│   ├── protocols/                      Interfaces and channel infrastructure
│   │   ├── CatalogueProtocol.java      Browse, search, product detail, delivery
│   │   ├── CartProtocol.java           Cart operations (extends CatalogueProtocol)
│   │   ├── SavedProtocol.java          Save / wishlist operations (extends CatalogueProtocol)
│   │   ├── MyEcommerceProtocol.java    Composed interface (extends all three; Web driver only)
│   │   ├── Channel.java                Enum singleton — reads -Dchannel once per JVM
│   │   ├── DriverRegistry.java         Registry: Channel → Supplier<CatalogueProtocol>
│   │   └── DriverFactory.java          Factory methods: createCatalogue(), createCart(), createSaved()
│   │
│   └── steps/                          Cucumber step definitions
│       ├── BrowseProductsSteps.java
│       ├── FilterSteps.java
│       ├── ProductDetailSteps.java
│       ├── DeliveryOptionsSteps.java
│       ├── CartSteps.java
│       └── SavedWishlistSteps.java
│
└── driver/
    ├── web/
    │   └── MyEcommerceDriver.java      Selenium implementation (implements MyEcommerceProtocol)
    └── api/
        ├── MyEcommerceDriver.java      REST Assured implementation (implements CatalogueProtocol)
        ├── ApiProduct.java             Internal Jackson DTO
        ├── ApiProductDetail.java       Internal Jackson DTO
        └── ApiDeliveryOption.java      Internal Jackson DTO

src/test/resources/features/
    browse_products.feature
    filter_search.feature
    product_detail.feature
    cart.feature
    delivery_options.feature
    saved_wishlist.feature
```

---

## Architecture

### Four layers

```
Feature files (Gherkin)
        │
        ▼
Step definitions  (dsl/steps/)       ← pure wiring, no logic
        │  calls
        ▼
Protocol interfaces  (dsl/protocols/)  ← business vocabulary, no browser/HTTP concepts
        │  implemented by
        ▼
Channel drivers  (driver/web/ or driver/api/)  ← all Selenium / REST Assured details here
```

Step definitions depend only on protocol interfaces. They never import Selenium, REST Assured, or any channel-specific class. This means the same step file works unmodified across both channels.

### Dual-channel design

The active channel is selected via the `-Dchannel` system property. `Channel.current()` reads it once, validates it, and caches the result.

```
-Dchannel=Web   →   driver/web/MyEcommerceDriver   (Selenium + Chrome)
-Dchannel=API   →   driver/api/MyEcommerceDriver   (REST Assured + FastAPI backend)
```

`DriverRegistry` holds a `Map<Channel, Supplier<CatalogueProtocol>>`. Each driver self-registers in its `static {}` block. `DriverRegistry` loads the driver class on demand by naming convention (`driver.{channel-lowercase}.MyEcommerceDriver`) if it is not yet in the map.

`DriverFactory` exposes three typed factory methods:

| Method | Returns | Throws if channel doesn't support it |
|---|---|---|
| `createCatalogue()` | `CatalogueProtocol` | Never |
| `createCart()` | `CartProtocol` | `IllegalStateException` |
| `createSaved()` | `SavedProtocol` | `IllegalStateException` |

### Interface Segregation

`MyEcommerceProtocol` is split into three narrower interfaces:

```
CatalogueProtocol          — browse, search, product detail, delivery (both channels)
CartProtocol               — cart operations (Web only — localStorage)
SavedProtocol              — save / wishlist operations (Web only — localStorage)
MyEcommerceProtocol        — empty composed interface, implemented by the Web driver
```

Step classes declare the narrowest type they need. `ProductDetailSteps` holds a `Supplier<CartProtocol>` (lazy) alongside an eager `CatalogueProtocol` so the API channel never triggers cart instantiation.

### Domain objects

`dsl/domain/` contains plain Java records (`ProductCard`, `CartState`, `DeliveryState`, etc.). They are the only types that cross the protocol boundary — drivers map all channel-specific data (DOM attributes, JSON fields) to these records before returning. They never appear in driver internals; internal API DTOs (`ApiProduct`, etc.) live in `driver/api/` and are never exposed upward.

### Self-registering drivers

Each driver registers itself via a `static {}` block:

```java
// driver/web/MyEcommerceDriver.java
static {
    DriverRegistry.register(Channel.WEB, MyEcommerceDriver::new);
}

// driver/api/MyEcommerceDriver.java
static {
    DriverRegistry.register(Channel.API, MyEcommerceDriver::new);
    // also configures Jackson for snake_case ↔ camelCase mapping
}
```

No factory or registry file needs to be changed to add a new channel.

---

## Scenario coverage

| Feature | Total scenarios | `@api` eligible | Notes |
|---|---|---|---|
| Browse products | 3 | 2 | Loading spinner is UI-only |
| Product detail | 5 | 2 | Click/navigate/cart steps are UI-only |
| Delivery options | 6 | 4 | Radio selection and fallback are UI-only; 3 currently `@wip` |
| Product search | 2 | 0 | Search is client-side in React |
| Cart | 6 | 0 | Cart is localStorage only |
| Saved / wishlist | 5 | 0 | Save state is localStorage only |

Scenarios that require the Web channel carry only the feature-level `@web` tag. Scenarios that also run on the API channel carry `@api` at the scenario level.

---

## `@wip` tag

Scenarios tagged `@wip` are excluded from all runs. The default tag filter in `build.gradle.kts` is `not @wip`; the `acceptance-api` justfile recipe appends `and not @wip` when filtering for `@api`.

Currently `@wip`:

| Scenario | Reason |
|---|---|
| `@delivery_default` | Flaky in full suite — delivery section timing |
| `@delivery_select` | Flaky in full suite — radio `isSelected()` stale state |
| `@delivery_header` | Flaky in full suite — XPath text scan intermittently empty |

Fix tracked in `../.claude/plans/api-channel.md` §16.
