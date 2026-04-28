import { Before, After, setWorldConstructor, setDefaultTimeout } from "@cucumber/cucumber";
import { chromium, type Browser, type Page } from "playwright";
import { currentChannel } from "../dsl/protocols/channel";
import { setPageHolder, clearPageHolder } from "../driver/web/pageHolder";
import { MyWorld } from "../support/world";

// Eagerly import drivers so they self-register before the first scenario
import "../driver/web/myEcommerceDriver";
import "../driver/api/myEcommerceDriver";

setWorldConstructor(MyWorld);
setDefaultTimeout(30_000);

let browser: Browser | undefined;
let page: Page | undefined;

Before({ order: 1 }, async () => {
  if (currentChannel() !== "Web") return;
  const headed = process.env["HEADED"] === "true";
  browser = await chromium.launch({ headless: !headed });
  page = await browser.newPage();
  setPageHolder(page);
});

Before({ order: 2 }, async () => {
  if (currentChannel() !== "Web" || page === undefined) return;
  await page.goto("http://localhost:3001");
  await page.evaluate(() => {
    try {
      localStorage.removeItem("cart");
      localStorage.removeItem("cartItems");
      localStorage.removeItem("selectedDelivery");
    } catch (_) {}
  });
});

After(async () => {
  if (currentChannel() !== "Web") return;
  clearPageHolder();
  await page?.close();
  await browser?.close();
  page = undefined;
  browser = undefined;
});
