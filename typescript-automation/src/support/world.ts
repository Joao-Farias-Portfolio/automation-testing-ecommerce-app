import { World, type IWorldOptions } from "@cucumber/cucumber";
import type { CatalogueProtocol } from "../dsl/protocols/catalogueProtocol";
import type { CartProtocol } from "../dsl/protocols/cartProtocol";
import type { SavedProtocol } from "../dsl/protocols/savedProtocol";
import { createCatalogue, createCart, createSaved } from "../dsl/protocols/driverFactory";

export class MyWorld extends World {
  private _catalogue: CatalogueProtocol | undefined;
  private _cart: CartProtocol | undefined;
  private _saved: SavedProtocol | undefined;

  constructor(options: IWorldOptions) {
    super(options);
  }

  get catalogue(): CatalogueProtocol {
    this._catalogue ??= createCatalogue();
    return this._catalogue;
  }

  get cart(): CartProtocol {
    this._cart ??= createCart();
    return this._cart;
  }

  get saved(): SavedProtocol {
    this._saved ??= createSaved();
    return this._saved;
  }
}
