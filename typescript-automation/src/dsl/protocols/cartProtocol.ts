import type { CartState } from "../domain/index";
import type { CatalogueProtocol } from "./catalogueProtocol";

export interface CartProtocol extends CatalogueProtocol {
  viewCart(): Promise<void>;
  addProductToCart(): Promise<void>;
  removeFirstItemFromCart(): Promise<void>;
  changeQuantityTo(quantity: number): Promise<void>;

  getCartState(): Promise<CartState>;
}
