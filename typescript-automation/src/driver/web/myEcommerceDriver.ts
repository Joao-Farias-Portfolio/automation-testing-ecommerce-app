import type { BrowserPort } from "../ports/browserPort";
import type { MyEcommerceProtocol } from "../../dsl/protocols/myEcommerceProtocol";
import { register } from "../../dsl/protocols/driverRegistry";
import type {
  CartItem,
  CartState,
  DeliveryOption,
  DeliveryState,
  ProductCard,
  ProductDetail,
  ProductListing,
  SavedState,
  SearchResults,
} from "../../dsl/domain/index";
import { getPageHolder } from "./pageHolder";
import { PlaywrightBrowserPort } from "./playwrightBrowserPort";

const BASE_URL = "http://localhost:3001";

const DELIVERY_SELECTOR =
  "[data-testid='delivery-section'], [data-testid='delivery-options'], " +
  "[data-testid='shipping-section'], [data-testid='shipping-options']";

register("Web", () => new MyEcommerceDriver(new PlaywrightBrowserPort(getPageHolder())));

export class MyEcommerceDriver implements MyEcommerceProtocol {
  constructor(private readonly browser: BrowserPort) {}

  async browseCatalogue(): Promise<void> {
    await this.browser.navigateTo(BASE_URL);
  }

  async viewCart(): Promise<void> {
    await this.browser.navigateTo(`${BASE_URL}/cart`);
    await this.browser.waitUntilAnyPresent("[data-testid='cart-item']", "[data-testid='empty-cart']");
  }

  async viewSavedItems(): Promise<void> {
    await this.browser.navigateTo(`${BASE_URL}/saved`);
    await this.browser.waitUntilUrlContains("/saved");
  }

  async returnToProductListing(): Promise<void> {
    await this.browser.navigateBack();
  }

  async addProductToCart(): Promise<void> {
    await this.browser.waitUntilCountMoreThan("[data-testid='add-to-cart']", 0);
    const countBefore = await this.readCartCount();
    const buttonCount = await this.browser.count("[data-testid='add-to-cart']");
    let enabledIndex = 0;
    for (let i = 0; i < buttonCount; i++) {
      if (await this.browser.isNthEnabled("[data-testid='add-to-cart']", i)) {
        enabledIndex = i;
        break;
      }
    }
    await this.browser.clickNth("[data-testid='add-to-cart']", enabledIndex);
    await this.browser.waitUntilCondition(async () => (await this.readCartCount()) > countBefore, 10_000);
  }

  async removeFirstItemFromCart(): Promise<void> {
    await this.browser.waitUntilCountMoreThan("[data-testid='remove-item']", 0);
    const countBefore = await this.readCartCount();
    await this.browser.clickNth("[data-testid='remove-item']", 0);
    await this.browser.waitUntilCondition(
      async () =>
        (await this.readCartCount()) < countBefore || (await this.browser.isPresent("[data-testid='empty-cart']")),
      10_000
    );
  }

  async changeQuantityTo(quantity: number): Promise<void> {
    await this.browser.waitUntilPresent("[data-testid='quantity-display']");
    const totalBefore = await this.readCartTotal();
    await this.browser.setReactInputValue("[data-testid='quantity-display']", String(quantity));
    await this.browser.waitUntilCondition(async () => (await this.readCartTotal()) !== totalBefore, 10_000);
  }

  async searchFor(term: string): Promise<void> {
    await this.browser.sendKeys("input[placeholder*='Search Items']", term, true);
    await this.browser.waitUntilUrlContains("/search/");
  }

  async viewFirstProduct(): Promise<void> {
    await this.browser.waitUntilVisible("[data-testid='product-card']");
    await this.browser.clickNth("[data-testid='product-card']", 0);
    await this.browser.waitUntilUrlMatches(".*/products/\\d+");
  }

  async chooseAlternativeDeliveryOption(): Promise<void> {
    const radioCount = await this.browser.count("input[type='radio']");
    for (let i = 0; i < radioCount; i++) {
      if (!(await this.browser.isNthSelected("input[type='radio']", i))) {
        await this.browser.clickXpath(`(//input[@type='radio'])[${i + 1}]/../../..`);
        return;
      }
    }
  }

  async ensureFirstProductIsSaved(): Promise<void> {
    await this.browser.waitUntilVisible("[data-testid='save-button']");
    const pressed = (await this.browser.attribute("[data-testid='save-button']", "aria-pressed")) === "true";
    if (!pressed) await this.toggleSaveStateOfFirstProduct();
  }

  async toggleSaveStateOfFirstProduct(): Promise<void> {
    const previousState = await this.browser.attribute("[data-testid='save-button']", "aria-pressed");
    await this.browser.clickNth("[data-testid='save-button']", 0);
    await this.browser.waitUntilAttributeChanges("[data-testid='save-button']", 0, "aria-pressed", previousState);
  }

  async viewWishlist(): Promise<void> {
    await this.browser.click("[data-testid='wishlist-link']");
    await this.browser.waitUntilUrlContains("/saved");
  }

  async getProductListing(): Promise<ProductListing> {
    await this.browser.waitUntilVisible("[data-testid='product-card']");
    const cards = await this.extractProductCards();
    const loadingVisible =
      (await this.browser.isPresent("[data-testid='loading']")) &&
      (await this.browser.isVisible("[data-testid='loading']"));
    return { cards, hasVisibleLoadingIndicators: loadingVisible };
  }

  async getCartState(): Promise<CartState> {
    const itemCount = await this.readCartCount();
    const total = await this.readCartTotal();
    const items = await this.readCartItems();
    const isEmpty = await this.browser.isPresent("[data-testid='empty-cart']");
    return { itemCount, total, items, isEmpty };
  }

  async getProductDetail(): Promise<ProductDetail> {
    await this.browser.waitUntilVisible("[data-testid='product-title']");
    return {
      title: await this.browser.text("[data-testid='product-title']"),
      price: await this.browser.text("[data-testid='product-price']"),
      description: await this.browser.text("[data-testid='product-description']"),
      imagePresent: await this.browser.isPresent("[data-testid='product-detail-image']"),
      addToCartButtonText: await this.browser.text("[data-testid='add-to-cart']"),
      addToCartEnabled: await this.browser.isEnabled("[data-testid='add-to-cart']"),
    };
  }

  async getDeliveryState(): Promise<DeliveryState> {
    await this.browser.waitUntilVisible("[data-testid='product-title']");
    const found = await this.browser.tryWaitUntilPresent(DELIVERY_SELECTOR, 5_000);
    if (!found || !(await this.browser.isVisible(DELIVERY_SELECTOR))) {
      return { sectionVisible: false, options: [], headerText: "", minimumOrderTextPresent: false };
    }
    const radioCount = await this.browser.count("input[type='radio']");
    const options: ReadonlyArray<DeliveryOption> = await Promise.all(
      Array.from({ length: radioCount }, async (_, i) => {
        const id = await this.browser.nthAttribute("input[type='radio']", i, "value");
        const selected = await this.browser.isNthSelected("input[type='radio']", i);
        return { label: id === "" ? String(i) : id, selected };
      })
    );
    const header = await this.browser.nthText(`${DELIVERY_SELECTOR} p`, 0);
    const minimumOrderTextPresent = await this.browser.isPresent(
      "[data-testid='minimum-order'], [data-testid='min-order']"
    );
    return { sectionVisible: true, options, headerText: header, minimumOrderTextPresent };
  }

  async getSearchResults(): Promise<SearchResults> {
    await this.browser.waitUntilUrlContains("/search/");
    const cards = await this.extractProductCards();
    return { cards, emptyStateVisible: await this.browser.isPresent("[data-testid='no-results']") };
  }

  async getSavedState(): Promise<SavedState> {
    await this.browser.waitUntilAnyPresent(
      "[data-testid='product-card']",
      "[data-testid='save-button']",
      "[data-testid='wishlist-link']"
    );
    const saveButtonPresent = await this.browser.isPresent("[data-testid='save-button']");
    const saveButtonPressed =
      saveButtonPresent &&
      (await this.browser.attribute("[data-testid='save-button']", "aria-pressed")) === "true";
    const saveButtonEnabled = saveButtonPresent && (await this.browser.isEnabled("[data-testid='save-button']"));
    return {
      saveButtonPresent,
      saveButtonPressed,
      saveButtonEnabled,
      savedPageCount: await this.readSavedCount(),
      wishlistLinkVisible: await this.browser.isPresent("[data-testid='wishlist-link']"),
    };
  }

  async currentUrl(): Promise<string> {
    return this.browser.currentUrl();
  }

  private async extractProductCards(): Promise<ReadonlyArray<ProductCard>> {
    const raw = await this.browser.extractAllViaScript(`
      return Array.from(document.querySelectorAll('[data-testid="product-card"]'))
        .map(card => ({
          title: (card.querySelector('[data-testid="product-title"]')?.textContent ?? '').trim(),
          price: (card.querySelector('[data-testid="product-price"]')?.textContent ?? '').trim(),
          imageUrl: card.querySelector('img')?.src ?? ''
        }));
    `);
    return raw.map((m) => ({
      title: m["title"] ?? "",
      price: m["price"] ?? "",
      imageUrl: m["imageUrl"] ?? "",
    }));
  }

  private async readCartCount(): Promise<number> {
    const t = await this.browser.text("[data-testid='cart-count']");
    return t === "" ? 0 : parseInt(t, 10);
  }

  private readCartTotal(): Promise<string> {
    return this.browser.text("[data-testid='cart-total']");
  }

  private async readCartItems(): Promise<ReadonlyArray<CartItem>> {
    const n = await this.browser.count("[data-testid='cart-item']");
    return Promise.all(
      Array.from({ length: n }, async (_, i) => ({
        title: await this.browser.nthText("[data-testid='cart-item']", i),
      }))
    );
  }

  private async readSavedCount(): Promise<number> {
    const t = (await this.browser.text("[data-testid='saved-count']")).replace(/[^0-9]/g, "").trim();
    return t === "" ? 0 : parseInt(t, 10);
  }
}
