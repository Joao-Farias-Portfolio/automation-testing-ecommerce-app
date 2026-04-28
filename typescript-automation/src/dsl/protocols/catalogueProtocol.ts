import type { DeliveryState, ProductDetail, ProductListing, SearchResults } from "../domain/index";

export interface CatalogueProtocol {
  browseCatalogue(): Promise<void>;
  viewFirstProduct(): Promise<void>;
  searchFor(term: string): Promise<void>;
  returnToProductListing(): Promise<void>;
  chooseAlternativeDeliveryOption(): Promise<void>;

  getProductListing(): Promise<ProductListing>;
  getProductDetail(): Promise<ProductDetail>;
  getDeliveryState(): Promise<DeliveryState>;
  getSearchResults(): Promise<SearchResults>;
  currentUrl(): Promise<string>;
}
