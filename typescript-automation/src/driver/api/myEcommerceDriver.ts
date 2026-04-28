import type { HttpPort } from "../ports/httpPort";
import type { ApiDeliveryOption, ApiProduct, ApiProductDetail } from "./apiModels";
import type { CatalogueProtocol } from "../../dsl/protocols/catalogueProtocol";
import { register } from "../../dsl/protocols/driverRegistry";
import type { DeliveryOption, DeliveryState, ProductCard, ProductDetail, ProductListing, SearchResults } from "../../dsl/domain/index";
import { AxiosHttpPort } from "./axiosHttpPort";

const BASE_URL = "http://localhost:8001";

register("API", () => new MyEcommerceDriver(new AxiosHttpPort(BASE_URL)));

export class MyEcommerceDriver implements CatalogueProtocol {
  private currentProductId = -1;
  private lastSearchTerm = "";

  constructor(private readonly http: HttpPort) {}

  async browseCatalogue(): Promise<void> {}
  async returnToProductListing(): Promise<void> {}
  async chooseAlternativeDeliveryOption(): Promise<void> {}

  async viewFirstProduct(): Promise<void> {
    const products = await this.fetchProducts("");
    const first = products[0];
    if (first === undefined) throw new Error("No products available");
    this.currentProductId = first.id;
  }

  async searchFor(term: string): Promise<void> {
    this.lastSearchTerm = term;
  }

  async getProductListing(): Promise<ProductListing> {
    const cards = (await this.fetchProducts("")).map((p) => this.toProductCard(p));
    return { cards, hasVisibleLoadingIndicators: false };
  }

  async getProductDetail(): Promise<ProductDetail> {
    const product = await this.fetchProductDetail(this.currentProductId);
    return {
      title: product.title,
      price: this.formatPrice(product.price),
      description: product.description,
      imagePresent: product.image_url !== null && product.image_url !== "",
      addToCartButtonText: "Add to Cart",
      addToCartEnabled: true,
    };
  }

  async getDeliveryState(): Promise<DeliveryState> {
    const activeOptions = await this.fetchActiveDeliveryOptions();
    if (activeOptions.length === 0) {
      return { sectionVisible: false, options: [], headerText: "", minimumOrderTextPresent: false };
    }
    const options: ReadonlyArray<DeliveryOption> = activeOptions.map((opt, i) => ({
      label: opt.name,
      selected: i === 0,
    }));
    return { sectionVisible: true, options, headerText: "Delivery Options", minimumOrderTextPresent: false };
  }

  async getSearchResults(): Promise<SearchResults> {
    const cards = (await this.fetchProducts(this.lastSearchTerm)).map((p) => this.toProductCard(p));
    return { cards, emptyStateVisible: cards.length === 0 };
  }

  async currentUrl(): Promise<string> {
    if (this.lastSearchTerm !== "") return `${BASE_URL}/products?search=${this.lastSearchTerm}`;
    if (this.currentProductId >= 0) return `${BASE_URL}/products/${this.currentProductId}`;
    return `${BASE_URL}/products`;
  }

  private async fetchProducts(searchTerm: string): Promise<ReadonlyArray<ApiProduct>> {
    if (searchTerm === "") return this.http.getListAs<ApiProduct>("/products");
    return this.http.getListWithQueryAs<ApiProduct>("/products", "search", searchTerm);
  }

  private fetchProductDetail(id: number): Promise<ApiProductDetail> {
    return this.http.getAs<ApiProductDetail>(`/products/${id}`);
  }

  private async fetchActiveDeliveryOptions(): Promise<ReadonlyArray<ApiDeliveryOption>> {
    const detail = await this.fetchProductDetail(this.currentProductId);
    return detail.delivery_options.filter((o) => o.is_active);
  }

  private toProductCard(product: ApiProduct): ProductCard {
    const rawUrl = product.image_url;
    const imageUrl = rawUrl === null || rawUrl === ""
      ? ""
      : rawUrl.startsWith("http") ? rawUrl : `${BASE_URL}${rawUrl}`;
    return { title: product.title, price: this.formatPrice(product.price), imageUrl };
  }

  private formatPrice(price: number): string {
    return `$${price.toFixed(2)}`;
  }
}
