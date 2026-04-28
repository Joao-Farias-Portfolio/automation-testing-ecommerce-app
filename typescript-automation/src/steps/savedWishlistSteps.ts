import { Given, When, Then } from "@cucumber/cucumber";
import { expect } from "chai";
import type { MyWorld } from "../support/world";

let initialSaveState = false;

Given<MyWorld>("the shopper is on the homepage with save buttons visible", async function () {
  await this.saved.browseCatalogue();
});

Given<MyWorld>("the shopper records the initial save state of the first product", async function () {
  initialSaveState = (await this.saved.getSavedState()).saveButtonPressed;
});

Given<MyWorld>("the shopper has saved the first product", async function () {
  await this.saved.ensureFirstProductIsSaved();
});

Given<MyWorld>("the shopper is on the saved page", async function () {
  await this.saved.viewSavedItems();
});

When<MyWorld>("the shopper toggles the save button for the first product", async function () {
  await this.saved.toggleSaveStateOfFirstProduct();
});

When<MyWorld>("the shopper toggles the save button again", async function () {
  await this.saved.toggleSaveStateOfFirstProduct();
});

When<MyWorld>("the shopper navigates to the saved page", async function () {
  await this.saved.viewSavedItems();
});

When<MyWorld>("the shopper clicks the wishlist link", async function () {
  await this.saved.viewWishlist();
});

Then<MyWorld>("the save state of the first product should have changed", async function () {
  const current = (await this.saved.getSavedState()).saveButtonPressed;
  expect(current, `save state should have changed from initial state (${initialSaveState})`).to.not.equal(
    initialSaveState
  );
});

Then<MyWorld>("the save state should be restored to the initial state", async function () {
  const current = (await this.saved.getSavedState()).saveButtonPressed;
  expect(current, `save state should be restored to initial (${initialSaveState})`).to.equal(initialSaveState);
});

Then<MyWorld>("the saved count should be visible and show a number", async function () {
  expect(
    (await this.saved.getSavedState()).savedPageCount,
    "saved count should be >= 1"
  ).to.be.at.least(1);
});

Then<MyWorld>("the wishlist link should be visible", async function () {
  expect((await this.saved.getSavedState()).wishlistLinkVisible, "wishlist link should be visible").to.be.true;
});

Then<MyWorld>("the URL should contain \\/saved", async function () {
  expect(await this.saved.currentUrl(), "URL should contain '/saved'").to.include("/saved");
});

Then<MyWorld>("the save button should be visible and functional on the detail page", async function () {
  const state = await this.saved.getSavedState();
  expect(state.saveButtonPresent, "save button should be present").to.be.true;
  expect(state.saveButtonEnabled, "save button should be enabled").to.be.true;
});
