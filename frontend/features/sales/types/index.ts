export type PaymentMethod = "CASH" | "PIX" | "CREDIT_CARD" | "DEBIT_CARD";

export type SaleState =
  | "OPEN"
  | "ON_HOLD"
  | "PAYMENT_IN_PROGRESS"
  | "PAID"
  | "COMPLETED"
  | "CANCELED";

export type ProductCodeType = "INTERNAL_CODE" | "BAR_CODE";
export type FidelityDiscountType = "FIXED" | "PERCENTAGE";

export interface ProductResponse {
  id: string;
  name: string;
  stockQuantity: number;
  location?: string;
  warehouse?: string;
  internalCode: string;
  barcode: string | null;
  price: number;
  costPrice: number;
}

export interface SaleItemResponse {
  id: string;
  productName: string;
  productInternalCode: string;
  unitPrice: number;
  quantity: number;
  discount: number;
  subtotal: number;
}

export interface PaymentResponse {
  id: string;
  method: PaymentMethod;
  amount: number;
  changeAmount: number;
  confirmed: boolean;
  transactionId: string | null;
  createdAt: string;
}

export interface SaleResponse {
  id: string;
  sessionToken: string;
  state: SaleState;
  items: SaleItemResponse[];
  payments: PaymentResponse[];
  subtotal: number;
  total: number;
  amountDue: number;
  clientId: string | null;
  fidelityDiscountApplied: number;
}

export interface AddItemRequest {
  type: ProductCodeType;
  code: string;
  quantity: number;
}

export interface PaymentRequest {
  method: PaymentMethod;
  amount: number;
}

export interface ApplyDiscountRequest {
  itemId: string;
  discountAmount: number;
}
