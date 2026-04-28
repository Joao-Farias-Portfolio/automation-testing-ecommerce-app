import { Given, When, Then } from "@cucumber/cucumber";
import { expect } from "chai";
import type { MyWorld } from "../support/world";

let notedItemCount = 0;
let notedTotal = "";

Given<MyWorld>("the shopper notes the current cart count", async function () {
  notedItemCount = (await this.cart.getCartState()).itemCount;
});

When<MyWorld>("the shopper adds the product to the cart", async function () {
  await this.cart.addProductToCart();
});

When<MyWorld>("the shopper navigates to the cart page", async function () {
  await this.cart.viewCart();
});

When<MyWorld>("the shopper returns to the homepage", async function () {
  await this.cart.browseCatalogue();
});

When<MyWorld>("the shopper changes the quantity to {int}", async function (quantity: number) {
  await this.cart.changeQuantityTo(quantity);
});

When<MyWorld>("the shopper removes the first cart item", async function () {
  await this.cart.removeFirstItemFromCart();
});

When<MyWorld>("the shopper notes the current cart total", async function () {
  notedTotal = (await this.cart.getCartState()).total;
});

Then<MyWorld>("the cart badge should show {int} item(s)", async function (expectedCount: number) {
  expect((await this.cart.getCartState()).itemCount, `cart badge should show ${expectedCount} item(s)`).to.equal(
    expectedCount
  );
});

Then<MyWorld>("the cart badge should have increased by {int}", async function (increment: number) {
  const expected = notedItemCount + increment;
  expect((await this.cart.getCartState()).itemCount, `cart badge should have increased by ${increment}`).to.equal(
    expected
  );
});

Then<MyWorld>("the cart should contain at least {int} items", async function (minimum: number) {
  expect(
    (await this.cart.getCartState()).items.length,
    `cart should contain at least ${minimum} items`
  ).to.be.at.least(minimum);
});

Then<MyWorld>("the cart total should be visible and show a price", async function () {
  expect((await this.cart.getCartState()).total, "cart total should show a price with $").to.match(/.*\$\d+.*/);
});

Then<MyWorld>("the cart total should have changed", async function () {
  expect((await this.cart.getCartState()).total, `cart total should have changed from ${notedTotal}`).to.not.equal(
    notedTotal
  );
});

Then<MyWorld>("the cart should show an empty state", async function () {
  expect((await this.cart.getCartState()).isEmpty, "cart should show empty state").to.be.true;
});

Then<MyWorld>("the first cart item should be visible", async function () {
  expect((await this.cart.getCartState()).items, "cart should have at least one item").to.not.be.empty;
});
