export type ProductCard = {
  readonly title: string;
  readonly price: string;
  readonly imageUrl: string;
};

export type ProductListing = {
  readonly cards: ReadonlyArray<ProductCard>;
  readonly hasVisibleLoadingIndicators: boolean;
};

export type ProductDetail = {
  readonly title: string;
  readonly price: string;
  readonly description: string;
  readonly imagePresent: boolean;
  readonly addToCartButtonText: string;
  readonly addToCartEnabled: boolean;
};

export type CartItem = {
  readonly title: string;
};

export type CartState = {
  readonly itemCount: number;
  readonly total: string;
  readonly items: ReadonlyArray<CartItem>;
  readonly isEmpty: boolean;
};

export type DeliveryOption = {
  readonly label: string;
  readonly selected: boolean;
};

export type DeliveryState = {
  readonly sectionVisible: boolean;
  readonly options: ReadonlyArray<DeliveryOption>;
  readonly headerText: string;
  readonly minimumOrderTextPresent: boolean;
};

export type SearchResults = {
  readonly cards: ReadonlyArray<ProductCard>;
  readonly emptyStateVisible: boolean;
};

export type SavedState = {
  readonly saveButtonPresent: boolean;
  readonly saveButtonPressed: boolean;
  readonly saveButtonEnabled: boolean;
  readonly savedPageCount: number;
  readonly wishlistLinkVisible: boolean;
};

export function selectedOptionCount(state: DeliveryState): number {
  return state.options.filter((o) => o.selected).length;
}
