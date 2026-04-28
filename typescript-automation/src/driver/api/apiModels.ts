export type ApiProduct = {
  readonly id: number;
  readonly title: string;
  readonly description: string;
  readonly price: number;
  readonly image_url: string | null;
};

export type ApiDeliveryOption = {
  readonly id: number;
  readonly name: string;
  readonly description: string;
  readonly price: number;
  readonly is_active: boolean;
  readonly min_order_amount: number | null;
};

export type ApiProductDetail = {
  readonly id: number;
  readonly title: string;
  readonly description: string;
  readonly price: number;
  readonly image_url: string | null;
  readonly delivery_options: ReadonlyArray<ApiDeliveryOption>;
};
