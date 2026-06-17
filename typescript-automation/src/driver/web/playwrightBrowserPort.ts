import type { Page } from "playwright";
import type { BrowserPort } from "../ports/browserPort";

export class PlaywrightBrowserPort implements BrowserPort {
  constructor(private readonly page: Page) {}

  async navigateTo(url: string): Promise<void> {
    await this.page.goto(url);
  }

  async navigateBack(): Promise<void> {
    await this.page.goBack();
  }

  async currentUrl(): Promise<string> {
    return this.page.url();
  }

  async isVisible(css: string): Promise<boolean> {
    return this.page.locator(css).first().isVisible().catch(() => false);
  }

  async isPresent(css: string): Promise<boolean> {
    return (await this.page.locator(css).count()) > 0;
  }

  async isEnabled(css: string): Promise<boolean> {
    return this.page.locator(css).first().isEnabled().catch(() => false);
  }

  async isSelected(css: string): Promise<boolean> {
    return this.page.locator(css).first().isChecked().catch(() => false);
  }

  async count(css: string): Promise<number> {
    return this.page.locator(css).count();
  }

  async text(css: string): Promise<string> {
    const loc = this.page.locator(css).first();
    return (await loc.count()) === 0 ? "" : (await loc.textContent())?.trim() ?? "";
  }

  async attribute(css: string, attr: string): Promise<string> {
    const loc = this.page.locator(css).first();
    return (await loc.count()) === 0 ? "" : (await loc.getAttribute(attr)) ?? "";
  }

  async isNthEnabled(css: string, index: number): Promise<boolean> {
    const loc = this.page.locator(css).nth(index);
    return (await loc.count()) > 0 && await loc.isEnabled();
  }

  async isNthSelected(css: string, index: number): Promise<boolean> {
    const loc = this.page.locator(css).nth(index);
    return (await loc.count()) > 0 && await loc.isChecked().catch(() => false);
  }

  async nthAttribute(css: string, index: number, attr: string): Promise<string> {
    const loc = this.page.locator(css).nth(index);
    return (await loc.count()) === 0 ? "" : (await loc.getAttribute(attr)) ?? "";
  }

  async nthText(css: string, index: number): Promise<string> {
    const loc = this.page.locator(css).nth(index);
    return (await loc.count()) === 0 ? "" : (await loc.textContent())?.trim() ?? "";
  }

  async isSelectedWithin(parentCss: string, parentIndex: number, childCss: string): Promise<boolean> {
    const parent = this.page.locator(parentCss).nth(parentIndex);
    if ((await parent.count()) === 0) return false;
    const child = parent.locator(childCss).first();
    return (await child.count()) > 0 && child.isChecked().catch(() => false);
  }

  async attributeWithin(parentCss: string, parentIndex: number, childCss: string, attr: string): Promise<string> {
    const parent = this.page.locator(parentCss).nth(parentIndex);
    if ((await parent.count()) === 0) return "";
    const child = parent.locator(childCss).first();
    return (await child.count()) === 0 ? "" : (await child.getAttribute(attr)) ?? "";
  }

  async click(css: string): Promise<void> {
    await this.page.locator(css).first().click();
  }

  async clickNth(css: string, index: number): Promise<void> {
    await this.page.locator(css).nth(index).click();
  }

  async clickXpath(xpath: string): Promise<void> {
    await this.page.locator(`xpath=${xpath}`).first().click();
  }

  async sendKeys(css: string, text: string, submitAfter: boolean): Promise<void> {
    const loc = this.page.locator(css).first();
    await loc.clear();
    await loc.fill(text);
    if (submitAfter) await loc.press("Enter");
  }

  async setReactInputValue(css: string, value: string): Promise<void> {
    await this.page.evaluate(
      ({ selector, val }: { selector: string; val: string }) => {
        const input = document.querySelector(selector) as HTMLInputElement | null;
        if (!input) return;
        const setter = Object.getOwnPropertyDescriptor(HTMLInputElement.prototype, "value")?.set;
        setter?.call(input, val);
        input.dispatchEvent(new Event("input", { bubbles: true }));
        input.dispatchEvent(new Event("change", { bubbles: true }));
      },
      { selector: css, val: value }
    );
  }

  async extractAllViaScript(script: string): Promise<ReadonlyArray<Readonly<Record<string, string>>>> {
    const result = await this.page.evaluate(`(() => { ${script} })()`);
    return result as ReadonlyArray<Readonly<Record<string, string>>>;
  }

  async executeScript(script: string, ...args: unknown[]): Promise<unknown> {
    return this.page.evaluate(`((args) => { ${script} })(${JSON.stringify(args)})`);
  }

  async waitUntilVisible(css: string): Promise<void> {
    await this.page.locator(css).first().waitFor({ state: "visible", timeout: 10_000 });
  }

  async waitUntilPresent(css: string): Promise<void> {
    await this.page.locator(css).first().waitFor({ state: "attached", timeout: 10_000 });
  }

  async waitUntilCountMoreThan(css: string, count: number): Promise<void> {
    await this.page.waitForFunction(
      ({ sel, min }: { sel: string; min: number }) =>
        document.querySelectorAll(sel).length > min,
      { sel: css, min: count },
      { timeout: 10_000 }
    );
  }

  async waitUntilUrlContains(fragment: string): Promise<void> {
    await this.page.waitForURL((url) => url.toString().includes(fragment), { timeout: 10_000 });
  }

  async waitUntilUrlMatches(regex: string): Promise<void> {
    await this.page.waitForURL(new RegExp(regex), { timeout: 10_000 });
  }

  async waitUntilAttributeChanges(css: string, index: number, attr: string, previousValue: string): Promise<void> {
    await this.page.waitForFunction(
      ({ sel, idx, attribute, prev }: { sel: string; idx: number; attribute: string; prev: string }) => {
        const els = document.querySelectorAll(sel);
        const el = els[idx];
        return el !== undefined && el.getAttribute(attribute) !== prev;
      },
      { sel: css, idx: index, attribute: attr, prev: previousValue },
      { timeout: 5_000 }
    );
  }

  async waitUntilAnyPresent(...cssList: string[]): Promise<void> {
    await Promise.race(
      cssList.map((css) => this.page.locator(css).first().waitFor({ state: "attached", timeout: 10_000 }))
    );
  }

  async waitUntilCondition(condition: () => Promise<boolean>, timeoutMs: number): Promise<void> {
    const deadline = Date.now() + timeoutMs;
    while (Date.now() < deadline) {
      if (await condition()) return;
      await this.page.waitForTimeout(200);
    }
    throw new Error(`Condition not met within ${timeoutMs}ms`);
  }

  async tryWaitUntilPresent(css: string, timeoutMs: number): Promise<boolean> {
    return this.page
      .locator(css)
      .first()
      .waitFor({ state: "attached", timeout: timeoutMs })
      .then(() => true)
      .catch(() => false);
  }
}
