import { Given, When, Then } from "@cucumber/cucumber";
import { expect } from "chai";
import type { MyWorld } from "../support/world";

let capturedListingTitle = "";
let notedCartCount = 0;

Given<MyWorld>("the shopper is viewing the first product", async function () {
  await this.catalogue.browseCatalogue();
  const { cards } = await this.catalogue.getProductListing();
  capturedListingTitle = cards[0]?.title ?? "";
  await this.catalogue.viewFirstProduct();
});

Given<MyWorld>("the shopper notes the cart count on the detail page", async function () {
  notedCartCount = (await this.cart.getCartState()).itemCount;
});

When<MyWorld>("the shopper clicks the first product card", async function () {
  await this.catalogue.viewFirstProduct();
});

When<MyWorld>("the shopper adds the product to the cart from the detail page", async function () {
  await this.cart.addProductToCart();
});

When<MyWorld>("the shopper navigates back", async function () {
  await this.catalogue.returnToProductListing();
});

Then<MyWorld>("the URL should match the product detail pattern", async function () {
  expect(await this.catalogue.currentUrl(), "URL should match product detail pattern").to.match(/.*\/products\/\d+/);
});

Then<MyWorld>("the product title should be visible on the detail page", async function () {
  expect((await this.catalogue.getProductDetail()).title, "product title should be visible").to.not.be.empty;
});

Then<MyWorld>("the product detail page should show price, description and image", async function () {
  const detail = await this.catalogue.getProductDetail();
  expect(detail.price, "price should show a $ value").to.match(/.*\$\d+.*/);
  expect(detail.description, "description should not be blank").to.not.be.empty;
  expect(detail.imagePresent, "product image should be present").to.be.true;
});

Then<MyWorld>("the product title should match the one from the listing", async function () {
  const detail = await this.catalogue.getProductDetail();
  expect(detail.title, "detail title should contain the listing title").to.include(capturedListingTitle.trim());
});

Then<MyWorld>("the add to cart button should show Added to Cart and be disabled", async function () {
  const detail = await this.catalogue.getProductDetail();
  expect(detail.addToCartButtonText.toLowerCase(), "add-to-cart button text should say 'added to cart'").to.include(
    "added to cart"
  );
  expect(detail.addToCartEnabled, "add-to-cart button should be disabled after adding").to.be.false;
});

Then<MyWorld>("the shopper should be back on the product listing", async function () {
  const listing = await this.catalogue.getProductListing();
  expect(listing.cards, "product listing should be visible after navigating back").to.not.be.empty;
  expect(await this.catalogue.currentUrl(), "URL should be back on the listing").to.match(/.*\/(\?.*)?$/);
});
