import { Given, When, Then } from "@cucumber/cucumber";
import { expect } from "chai";
import { selectedOptionCount } from "../dsl/domain/index";
import type { MyWorld } from "../support/world";

let notedSelectedDeliveryOption = "";

Given<MyWorld>("the shopper has navigated to a product detail page", async function () {
  await this.catalogue.browseCatalogue();
  await this.catalogue.viewFirstProduct();
});

When<MyWorld>("the shopper notes the currently selected delivery option", async function () {
  const state = await this.catalogue.getDeliveryState();
  const selected = state.options.find((o) => o.selected);
  if (selected === undefined) throw new Error("No delivery option is currently selected");
  notedSelectedDeliveryOption = selected.label;
});

When<MyWorld>("the shopper selects a different delivery option", async function () {
  await this.catalogue.chooseAlternativeDeliveryOption();
});

Then<MyWorld>("the delivery section should be visible", async function () {
  expect((await this.catalogue.getDeliveryState()).sectionVisible, "delivery section should be visible").to.be.true;
});

Then<MyWorld>("the delivery section should contain radio button options", async function () {
  expect((await this.catalogue.getDeliveryState()).options, "delivery section should contain options").to.not.be.empty;
});

Then<MyWorld>("one delivery option should be selected by default", async function () {
  const state = await this.catalogue.getDeliveryState();
  expect(state.sectionVisible, "delivery section must be visible").to.be.true;
  expect(selectedOptionCount(state), "exactly one delivery option should be selected by default").to.equal(1);
});

Then<MyWorld>("a different delivery option should now be selected", async function () {
  const state = await this.catalogue.getDeliveryState();
  expect(state.sectionVisible, "delivery section must be visible").to.be.true;
  expect(selectedOptionCount(state), "exactly one option should be selected after changing").to.equal(1);
  const nowSelected = state.options.find((o) => o.selected);
  if (nowSelected === undefined) throw new Error("No delivery option is selected after changing");
  expect(nowSelected.label, `selected option should have changed from '${notedSelectedDeliveryOption}'`).to.not.equal(
    notedSelectedDeliveryOption
  );
});

Then<MyWorld>("no minimum order restrictions should be shown", async function () {
  expect(
    (await this.catalogue.getDeliveryState()).minimumOrderTextPresent,
    "no minimum order restrictions should be shown"
  ).to.be.false;
});

Then<MyWorld>("the delivery section should have a header with delivery options text", async function () {
  expect((await this.catalogue.getDeliveryState()).headerText, "delivery section header should not be blank").to.not.be
    .empty;
});

Then<MyWorld>("the product detail page should still be functional without delivery options", async function () {
  const detail = await this.catalogue.getProductDetail();
  expect(detail.title, "product title should be present").to.not.be.empty;
  expect(detail.price, "product price should be present").to.not.be.empty;
  expect(detail.addToCartButtonText, "add-to-cart button should be present").to.not.be.empty;
});
