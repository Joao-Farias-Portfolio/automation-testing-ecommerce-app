# Plan: Java Automation Project — Serenity BDD + Selenium 4 + Gradle

## Technology Decisions

### Selenium 4 over Playwright for Java
Serenity's `serenity-selenium` module is first-class; `serenity-playwright-java` is a thin community wrapper with no Screenplay parity and sparse docs. Selenium 4 brings BiDi (CDP), native relative locators, and stable W3C protocol — all directly supported by Serenity 4.x's `WebElementFacade`, `PageObject`, and `Screenplay` layers.

### Gradle over Maven
Using Gradle 9.4.1 with Kotlin DSL. `serenity-gradle-plugin` version 5.3.9 adds the `aggregate` task that generates the HTML report after tests. Kotlin DSL provides type-safe build configuration and IDE completion.

### Versions
| Dependency | Version |
|---|---|
| Java | 25 |
| Gradle | 9.4.1 |
| Serenity BDD BOM | 5.3.10 |
| Serenity Gradle Plugin | 5.3.9 |
| WebDriverManager | 6.1.0 |
| AssertJ | 3.27.7 |
| JUnit Vintage Engine | 5.14.3 |

---

## 1. Folder Structure

```
java-automation/
├── build.gradle.kts
├── settings.gradle.kts
├── serenity.conf
├── .gitignore
├── gradle/wrapper/
│   └── gradle-wrapper.properties
└── src/
    └── test/
        ├── java/com/lineasupply/automation/
        │   ├── runner/
        │   │   └── CucumberTestSuite.java
        │   ├── stepdefinitions/
        │   │   ├── Hooks.java
        │   │   ├── BrowseProductsSteps.java
        │   │   ├── CartSteps.java
        │   │   ├── FilterSteps.java
        │   │   ├── ProductDetailSteps.java
        │   │   ├── DeliveryOptionsSteps.java
        │   │   └── SavedWishlistSteps.java
        │   ├── screenplay/
        │   │   ├── actors/OnlineShopper.java
        │   │   ├── tasks/
        │   │   │   ├── NavigateTo.java
        │   │   │   ├── AddProductToCart.java
        │   │   │   ├── RemoveItemFromCart.java
        │   │   │   ├── ChangeQuantity.java
        │   │   │   ├── SearchForProduct.java
        │   │   │   ├── SelectDeliveryOption.java
        │   │   │   └── ToggleSaveProduct.java
        │   │   └── questions/
        │   │       ├── CartItemCount.java
        │   │       ├── ProductsVisible.java
        │   │       ├── CartTotal.java
        │   │       └── SaveButtonState.java
        │   └── pages/
        │       ├── HomePage.java
        │       ├── ProductDetailPage.java
        │       ├── CartPage.java
        │       ├── SearchResultsPage.java
        │       └── SavedPage.java
        └── resources/features/
            ├── browse_products.feature
            ├── cart.feature
            ├── filter_search.feature
            ├── product_detail.feature
            ├── delivery_options.feature
            └── saved_wishlist.feature
```

Report output (git-ignored): `java-automation/build/reports/serenity/index.html`

---

## 2. `build.gradle.kts`

```kotlin
plugins {
    java
    id("net.serenity-bdd.serenity-gradle-plugin") version "5.3.9"
}

repositories {
    mavenCentral()
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}

dependencies {
    testImplementation(platform("net.serenity-bdd:serenity-bom:5.3.10"))
    testImplementation("net.serenity-bdd:serenity-core")
    testImplementation("net.serenity-bdd:serenity-selenium")
    testImplementation("net.serenity-bdd:serenity-screenplay")
    testImplementation("net.serenity-bdd:serenity-screenplay-webdriver")
    testImplementation("net.serenity-bdd:serenity-cucumber")
    testImplementation("io.github.bonigarcia:webdrivermanager:6.1.0")
    testImplementation("org.assertj:assertj-core:3.27.7")
    testImplementation("org.junit.vintage:junit-vintage-engine:5.14.3")
}

tasks.named<Test>("test") {
    useJUnitPlatform()
    include("**/*Suite.class")
    systemProperty("cucumber.filter.tags", System.getProperty("cucumber.filter.tags", "not @wip"))
    finalizedBy("aggregate")
}
```

---

## 3. `serenity.conf`

```hocon
serenity {
  project.name = "Linea Supply E2E - Java"
  take.screenshots = FOR_FAILURES
}

webdriver {
  driver = chrome
  capabilities {
    browserName = "chrome"
    "goog:chromeOptions" {
      args = ["--headless=new", "--no-sandbox", "--disable-dev-shm-usage",
              "--window-size=1920,1080", "--disable-extensions"]
    }
  }
}

environments {
  default { webdriver.base.url = "http://localhost:3001" }
  headed  {
    webdriver.capabilities."goog:chromeOptions".args = [
      "--window-size=1920,1080", "--disable-extensions"
    ]
  }
}
```

Run headed: `./gradlew test -Denvironment=headed`

---

## 4. Runner Class

```java
@RunWith(CucumberWithSerenity.class)
@CucumberOptions(
    features = "src/test/resources/features",
    glue    = "com.lineasupply.automation",
    tags    = "not @wip",
    plugin  = {"pretty"}
)
public class CucumberTestSuite {}
```

`CucumberWithSerenity` wires Serenity's listener, screenshots, and step reporting.
`cucumber.filter.tags` system property overrides the default tags at runtime.

---

## 5. Architecture Patterns

### Hooks
- `@Before(order=1)` — `OnStage.setTheStage(Cast.ofStandardActors())`
- `@Before(order=2)` — Navigate to app and clear localStorage (cart, cartItems, selectedDelivery)
- `@After` — `OnStage.drawTheCurtain()`

### Page Objects
- Extend `net.serenitybdd.core.pages.PageObject`
- Fields use `@FindBy` returning `WebElementFacade` (fluent, auto-waiting)
- Locators reuse `data-testid` attributes from the Playwright tests (same DOM)

### Screenplay
- **Tasks** — multi-step interactions using `Open`, `Click`, `Enter` built-in actions
- **Questions** — read-only, return a value; logged as named steps in Serenity report
- Actors obtained via `OnStage.theActorCalled("Shopper")` in step definitions
- For nth-element access: XPath `(//*[@data-testid='...'])[n]`

### Feature files
One per domain area, mirroring Playwright specs. Tags: `@browse`, `@cart`, `@filter`, `@detail`, `@delivery`, `@saved`, `@smoke`.

---

## 6. `justfile` Additions

```just
# ─── java-automation ──────────────────────────────────────────────────────────

install-java:
    @which gradle > /dev/null || (echo "Install Gradle: brew install gradle" && exit 1)
    cd java-automation && gradle wrapper --gradle-version 9.4.1
    @echo "Gradle wrapper created. Run: just test-java"

test-java:
    cd java-automation && ./gradlew test
    @echo "Report: java-automation/build/reports/serenity/index.html"

test-java-headed:
    cd java-automation && ./gradlew test -Denvironment=headed

test-java-tag TAG:
    cd java-automation && ./gradlew test -Dcucumber.filter.tags="{{TAG}}"

test-java-feature FEATURE:
    cd java-automation && ./gradlew test -Dcucumber.filter.tags="@{{FEATURE}}"

report-java:
    @open java-automation/build/reports/serenity/index.html

test-all:
    @just test-local
    @just test-e2e
    @just test-java
```

---

## 7. Running a Single Test

```bash
just test-java-tag TAG=@smoke
just test-java-tag TAG="@cart and not @wip"
just test-java-feature FEATURE=cart
just test-java-headed
```

---

## 8. Required Setup

```bash
# 1. Install Java 25 (if not present)
sdk install java 25-open && sdk use java 25-open

# 2. Install Gradle and generate wrapper (one-time)
brew install gradle
just install-java

# 3. Start the app
just dev-headless

# 4. Run tests
just test-java

# 5. Open report
just report-java
```

`webdrivermanager:6.1.0` auto-downloads the matching ChromeDriver at runtime — no manual setup needed.

---

## 9. `.gitignore` for `java-automation/`

```
build/
.gradle/
*.class
*.log
```

---

## Design Decisions Summary

| Decision | Choice | Reason |
|---|---|---|
| UI driver | Selenium 4 | First-class Serenity support; `WebElementFacade` auto-waits |
| Build tool | Gradle 9.4.1 (Kotlin DSL) | Modern, type-safe; `serenity-gradle-plugin` 5.3.9 handles reporting |
| Test pattern | Screenplay + Page Objects | Tasks/Questions become named steps in Serenity's living docs |
| Cucumber version | 7.x (via BOM) | Serenity 4.x ships with Cucumber 7 |
| Java version | 25 | Latest release; configured via Java toolchain in Gradle |
| Selector strategy | `data-testid` CSS / XPath | Reuses same attributes as existing Playwright tests |
| Cart state reset | JS `localStorage` clear in `@Before` | Mirrors Playwright's `beforeEach` pattern |
| Report location | `build/reports/serenity/index.html` | Gradle build output directory |
