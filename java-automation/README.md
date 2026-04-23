# java-automation

Serenity BDD + Cucumber acceptance tests for the Linea Supply e-commerce app. The same Gherkin scenarios run against two interchangeable **channels** (real browser or backend HTTP API) and — within the Web channel — two interchangeable **browser automation libraries** (Selenium or Playwright) and two interchangeable **HTTP clients** (REST Assured or OkHttp). No scenario or step definition knows which combination it is running against.

---

## CI / CD

- **CI pipeline definition:** [`/.github/workflows/ci.yml`](https://github.com/Joao-Farias-Portfolio/automation-testing-ecommerce-app/blob/main/.github/workflows/ci.yml)
- **CI run history:** [GitHub Actions](https://github.com/Joao-Farias-Portfolio/automation-testing-ecommerce-app/actions)
- **Performance tests:** [`/performance/`](https://github.com/Joao-Farias-Portfolio/automation-testing-ecommerce-app/tree/main/performance)

---

## How to run

**Prerequisites:** Java 21+, Gradle wrapper (`./gradlew`), Chrome installed. For the API channel, the FastAPI backend must be on port 8001 (`just dev-headless` from the repo root).

### Recommended — justfile recipes

```bash
# Web channel — 27 scenarios in a real Chrome browser (Selenium, headless)
just acceptance-web

# API channel — 8 scenarios directly against the backend HTTP API (REST Assured)
just acceptance-api

# Web channel with Playwright instead of Selenium
just acceptance-web-playwright

# Web channel with Playwright, headed browser (debugging)
just acceptance-web-playwright-headed

# API channel with OkHttp instead of REST Assured
just acceptance-api-okhttp

# Open the Serenity HTML report after any run
just report-java
```

### Direct Gradle invocation

```bash
# Channel
./gradlew test -Dchannel=Web
./gradlew test -Dchannel=API -Dcucumber.filter.tags="@api and not @wip"

# Tag filtering
./gradlew test -Dchannel=Web -Dcucumber.filter.tags="@smoke"
./gradlew test -Dchannel=Web -Dcucumber.filter.tags="@cart"

# Pluggable adapters (can be combined freely)
./gradlew test -Dchannel=Web -Dbrowser.impl=playwright
./gradlew test -Dchannel=Web -Dbrowser.impl=playwright -Dheaded=true
./gradlew test -Dchannel=API -Dhttp.impl=okhttp -Dcucumber.filter.tags="@api and not @wip"
```

### All system properties

| Property | Values | Default | Effect |
|---|---|---|---|
| `-Dchannel` | `Web`, `API` | — (required) | Selects the channel driver |
| `-Dbrowser.impl` | `selenium`, `playwright` | `selenium` | Selects the `BrowserPort` implementation (Web channel only) |
| `-Dhttp.impl` | `restassured`, `okhttp` | `restassured` | Selects the `HttpPort` implementation (API channel only) |
| `-Dheaded` | `true`, `false` | `false` | Runs the browser in headed mode |
| `-Dcucumber.filter.tags` | any Cucumber tag expression | `not @wip` | Filters which scenarios execute |

Scenarios tagged `@wip` are excluded from all runs by default.

---

## Package structure

```
src/test/java/com/myecommerce/automation/
│
├── runner/
│   └── CucumberTestSuite.java          JUnit Platform Suite + Serenity reporter
│
├── hooks/
│   └── Hooks.java                      @Before / @After: browser lifecycle (Web channel only)
│                                       Branches on -Dbrowser.impl to start Selenium or Playwright
│
├── dsl/
│   ├── domain/                         Plain Java records — shared by all channels and adapters
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
    ├── ports/                          Technology-agnostic interfaces (the seams)
    │   ├── BrowserPort.java            44 methods: navigation, queries, actions, waits
    │   └── HttpPort.java               3 methods: getAs, getListAs, getListWithQueryAs
    │
    ├── web/
    │   ├── MyEcommerceDriver.java      Implements MyEcommerceProtocol; depends only on BrowserPort
    │   ├── SeleniumBrowserPort.java    BrowserPort → Selenium WebDriver + Serenity Screenplay
    │   ├── PlaywrightBrowserPort.java  BrowserPort → Playwright Java (com.microsoft.playwright)
    │   └── PlaywrightPageHolder.java   ThreadLocal<Page> — hands the Playwright Page from Hooks to the driver
    │
    └── api/
        ├── MyEcommerceDriver.java      Implements CatalogueProtocol; depends only on HttpPort
        ├── RestAssuredHttpPort.java    HttpPort → SerenityRest + Jackson (appears in Serenity reports)
        ├── OkHttpHttpPort.java         HttpPort → OkHttp + Jackson (lightweight, no Serenity report integration)
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
Step definitions  (dsl/steps/)          ← pure wiring, no logic
        │  calls
        ▼
Protocol interfaces  (dsl/protocols/)   ← business vocabulary, no browser/HTTP concepts
        │  implemented by
        ▼
Channel drivers  (driver/web/ or driver/api/)
        │  depends on
        ▼
Port interfaces  (driver/ports/)        ← technology-agnostic seams
        │  implemented by
        ▼
Adapters  (SeleniumBrowserPort, PlaywrightBrowserPort, RestAssuredHttpPort, OkHttpHttpPort)
```

Step definitions depend only on protocol interfaces and never import Selenium, Playwright, REST Assured, OkHttp, or any channel-specific class. Channel drivers depend only on the port interfaces, not on any specific library. Library dependencies are confined to the four adapter classes.

### Dual-channel design

The active channel is selected via `-Dchannel`. `Channel.current()` reads it once, validates it, and caches the result.

```
-Dchannel=Web   →   driver/web/MyEcommerceDriver   (browser automation via BrowserPort)
-Dchannel=API   →   driver/api/MyEcommerceDriver   (HTTP calls via HttpPort)
```

`DriverRegistry` holds a `Map<Channel, Supplier<CatalogueProtocol>>`. Each driver self-registers in its `static {}` block, which reads the relevant `-D` property and selects the appropriate adapter:

```java
// driver/web/MyEcommerceDriver.java
static {
    DriverRegistry.register(Channel.WEB, () -> {
        BrowserPort browser = "playwright".equals(System.getProperty("browser.impl", "selenium"))
            ? PlaywrightBrowserPort.fromCurrentPage()
            : new SeleniumBrowserPort();
        return new MyEcommerceDriver(browser);
    });
}

// driver/api/MyEcommerceDriver.java
static {
    DriverRegistry.register(Channel.API, () -> {
        HttpPort http = "okhttp".equals(System.getProperty("http.impl", "restassured"))
            ? new OkHttpHttpPort(BASE_URL)
            : new RestAssuredHttpPort(BASE_URL);
        return new MyEcommerceDriver(http);
    });
}
```

`DriverFactory` exposes three typed factory methods:

| Method | Returns | Throws if channel doesn't support it |
|---|---|---|
| `createCatalogue()` | `CatalogueProtocol` | Never |
| `createCart()` | `CartProtocol` | `IllegalStateException` (API channel) |
| `createSaved()` | `SavedProtocol` | `IllegalStateException` (API channel) |

### Interface Segregation

`MyEcommerceProtocol` is split into three narrower interfaces:

```
CatalogueProtocol      browse, search, product detail, delivery  (both channels)
CartProtocol           cart operations                           (Web only — localStorage)
SavedProtocol          save / wishlist operations               (Web only — localStorage)
MyEcommerceProtocol    empty composed interface, Web driver only
```

Step classes declare the narrowest type they need. Classes that use `CartProtocol` or `SavedProtocol` hold a `Supplier<T>` (lazy) so the API channel never triggers instantiation of unsupported protocols.

### Port/Adapter pattern

`BrowserPort` and `HttpPort` are the two seams between channel drivers and infrastructure libraries. Adding a new library (e.g. a third browser library) requires only:

1. A new class implementing the relevant port interface.
2. A one-line branch in the driver's `static {}` block.

Nothing else changes.

#### `BrowserPort` (44 methods)

Covers: navigation (`navigateTo`, `navigateBack`, `currentUrl`), element state queries (`isVisible`, `isPresent`, `isEnabled`, `isSelected`, `count`), element content (`text`, `attribute`), nth-element queries (`isNthEnabled`, `isNthSelected`, `nthAttribute`, `nthText`), scoped queries within a parent element, actions (`click`, `clickNth`, `clickXpath`, `sendKeys`, `setReactInputValue`), JavaScript execution (`extractAllViaScript`, `executeScript`), and waits (`waitUntilVisible`, `waitUntilPresent`, `waitUntilCountMoreThan`, `waitUntilUrlContains`, `waitUntilUrlMatches`, `waitUntilAttributeChanges`, `waitUntilAnyPresent`, `waitUntilCondition`).

#### `HttpPort` (3 methods)

Covers: `getAs(path, type)`, `getListAs(path, elementType)`, `getListWithQueryAs(path, paramName, paramValue, elementType)`.

### Domain objects

`dsl/domain/` contains plain Java records (`ProductCard`, `CartState`, `DeliveryState`, etc.). They are the only types that cross the protocol boundary. Drivers map all channel-specific data (DOM attributes, JSON fields) to these records before returning. Internal API DTOs (`ApiProduct`, etc.) live in `driver/api/` and are never exposed upward.

### Adapter trade-offs

| | Selenium | Playwright |
|---|---|---|
| Serenity Screenplay integration | Full (`OnStage`, actor model, `@Step` reports) | None — plain `Page` object |
| Serenity screenshot on failure | Automatic | Requires manual `page.screenshot()` in `@After` |
| Driver management | `WebDriverManager.chromedriver()` | Self-contained via `playwright.chromium()` |

| | REST Assured | OkHttp |
|---|---|---|
| Serenity report integration | Yes — HTTP steps appear in Serenity HTML report | No — calls are invisible to Serenity |
| API | Fluent `.given().get().then().statusCode()` | Explicit `Request` / `Response` objects |
| Jackson setup | Via `RestAssured.config` global | Per-instance `ObjectMapper` |

---

## Scenario coverage

| Feature | Total scenarios | `@api` eligible | Notes |
|---|---|---|---|
| Browse products | 3 | 2 | Loading spinner scenario is UI-only |
| Product detail | 5 | 2 | Click/navigate/cart steps are UI-only |
| Delivery options | 6 | 4 | Radio selection and fallback scenarios are UI-only |
| Product search | 2 | 0 | Search is client-side React (no backend endpoint) |
| Cart | 6 | 0 | Cart is localStorage only |
| Saved / wishlist | 5 | 0 | Save state is localStorage only |
| **Total** | **27** | **8** | |

Scenarios that only run on the Web channel carry the feature-level `@web` tag. Scenarios that also run on the API channel carry `@api` at the scenario level.

---

## `@wip` tag

Scenarios tagged `@wip` are excluded from all runs. The default tag filter in `build.gradle.kts` is `not @wip`; the `acceptance-api` justfile recipe appends `and not @wip` when filtering for `@api`.

There are no scenarios currently tagged `@wip`.
