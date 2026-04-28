import { Given, When, Then } from "@cucumber/cucumber";
import { expect } from "chai";
import type { MyWorld } from "../support/world";

let capturedSearchTerm = "";

Given<MyWorld>("the shopper is on the homepage with products visible", async function () {
  await this.catalogue.browseCatalogue();
});

When<MyWorld>("the shopper searches for the first product name", async function () {
  const { cards } = await this.catalogue.getProductListing();
  expect(cards, "product cards must be visible to search").to.not.be.empty;
  capturedSearchTerm = (cards[0]?.title ?? "").split(" ")[0] ?? "";
  await this.catalogue.searchFor(capturedSearchTerm);
});

When<MyWorld>("the shopper searches for {string}", async function (term: string) {
  capturedSearchTerm = term;
  await this.catalogue.searchFor(term);
});

Then<MyWorld>("the URL should contain the search term", async function () {
  expect(await this.catalogue.currentUrl(), "URL should contain the searched term").to.include(
    `/search/${capturedSearchTerm}`
  );
});

Then<MyWorld>("search results should be displayed", async function () {
  expect((await this.catalogue.getSearchResults()).cards, "search results should show at least one card").to.not.be
    .empty;
});

Then<MyWorld>("no results or empty state should be shown", async function () {
  const results = await this.catalogue.getSearchResults();
  expect(results.emptyStateVisible || results.cards.length === 0, "either no-results or zero cards expected").to.be
    .true;
});
