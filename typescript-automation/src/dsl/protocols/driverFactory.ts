import { currentChannel } from "./channel";
import type { CatalogueProtocol } from "./catalogueProtocol";
import type { CartProtocol } from "./cartProtocol";
import type { SavedProtocol } from "./savedProtocol";
import { create } from "./driverRegistry";

export function createCatalogue(): CatalogueProtocol {
  return create(currentChannel());
}

export function createCart(): CartProtocol {
  const driver = create(currentChannel());
  if (!isCartProtocol(driver)) {
    throw new Error(`Channel '${currentChannel()}' does not support cart operations`);
  }
  return driver;
}

export function createSaved(): SavedProtocol {
  const driver = create(currentChannel());
  if (!isSavedProtocol(driver)) {
    throw new Error(`Channel '${currentChannel()}' does not support saved/wishlist operations`);
  }
  return driver;
}

function isCartProtocol(d: CatalogueProtocol): d is CartProtocol {
  return "viewCart" in d && "addProductToCart" in d && "getCartState" in d;
}

function isSavedProtocol(d: CatalogueProtocol): d is SavedProtocol {
  return "viewSavedItems" in d && "getSavedState" in d;
}
