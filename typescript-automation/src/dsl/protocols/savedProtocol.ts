import type { SavedState } from "../domain/index";
import type { CatalogueProtocol } from "./catalogueProtocol";

export interface SavedProtocol extends CatalogueProtocol {
  viewSavedItems(): Promise<void>;
  ensureFirstProductIsSaved(): Promise<void>;
  toggleSaveStateOfFirstProduct(): Promise<void>;
  viewWishlist(): Promise<void>;

  getSavedState(): Promise<SavedState>;
}
