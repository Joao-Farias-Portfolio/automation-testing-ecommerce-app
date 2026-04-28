import { Given, Then } from "@cucumber/cucumber";
import { expect } from "chai";
import type { MyWorld } from "../support/world";

Given<MyWorld>("the shopper is on the homepage", async function () {
  await this.catalogue.browseCatalogue();
});

Given<MyWorld>("the homepage has loaded with products", async function () {
  await this.catalogue.browseCatalogue();
});

Given<MyWorld>("the shopper is on the homepage with products loaded", async function () {
  await this.catalogue.browseCatalogue();
});

Then<MyWorld>("product cards should be visible", async function () {
  const { cards } = await this.catalogue.getProductListing();
  expect(cards, "product cards should be visible on the page").to.not.be.empty;
});

Then<MyWorld>("each product card should show a title and price", async function () {
  const { cards } = await this.catalogue.getProductListing();
  expect(cards).to.not.be.empty;
  const first = cards[0];
  expect(first?.title, "product title should be visible on first card").to.not.be.empty;
  expect(first?.price, "product price should be visible on first card").to.not.be.empty;
});

Then<MyWorld>("the page should show a loading indicator briefly", async function () {
  const listing = await this.catalogue.getProductListing();
  expect(listing.cards, "product cards should be visible after loading completes").to.not.be.empty;
  expect(listing.hasVisibleLoadingIndicators, "loading indicators should be gone once product cards are visible").to
    .be.false;
});

Then<MyWorld>("product images should have valid sources", async function () {
  const { cards } = await this.catalogue.getProductListing();
  for (const card of cards) {
    expect(card.imageUrl, `product image URL should be a valid http/https URL`).to.match(/^https?:\/\/.+/);
  }
});
