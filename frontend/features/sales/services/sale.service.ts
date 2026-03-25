import { api } from "@/shared/services/api";
import type {
  AddItemRequest,
  ApplyDiscountRequest,
  PaymentMethod,
  PaymentRequest,
  ProductCodeType,
  SaleResponse,
} from "../types";

const BASE = "/api/sales";

export const saleService = {
  getSale: (sessionToken: string): Promise<SaleResponse> =>
    api.get<SaleResponse>(`${BASE}/${sessionToken}`),

  createSale: (sessionToken: string): Promise<SaleResponse> =>
    api.post<SaleResponse>(`${BASE}/${sessionToken}`),

  addItem: (
    sessionToken: string,
    type: ProductCodeType,
    code: string,
  ): Promise<SaleResponse> =>
    api.post<SaleResponse>(`${BASE}/${sessionToken}/items`, {
      type,
      code,
    } satisfies AddItemRequest),

  removeItem: (
    sessionToken: string,
    productCode: string,
    type: ProductCodeType,
    operatorId: string,
    authorizerId?: string,
  ): Promise<void> => {
    const headers: Record<string, string> = {
      "X-Operator-Id": operatorId,
    };
    if (authorizerId) {
      headers["X-Authorizer-Id"] = authorizerId;
    }
    return api.delete<void>(
      `${BASE}/${sessionToken}/items/${productCode}`,
      { type },
      headers,
    );
  },

  cancelSale: (
    sessionToken: string,
    operatorId: string,
    authorizerId?: string,
  ): Promise<void> => {
    const headers: Record<string, string> = {
      "X-Operator-Id": operatorId,
    };
    if (authorizerId) {
      headers["X-Authorizer-Id"] = authorizerId;
    }
    return api.delete<void>(`${BASE}/${sessionToken}`, undefined, headers);
  },

  addPayment: (
    sessionToken: string,
    method: PaymentMethod,
    amount: number,
  ): Promise<SaleResponse> =>
    api.post<SaleResponse>(`${BASE}/${sessionToken}/payments`, {
      method,
      amount,
    } satisfies PaymentRequest),

  completeSale: (sessionToken: string): Promise<void> =>
    api.post<void>(`${BASE}/${sessionToken}/complete`),

  applyDiscount: (
    sessionToken: string,
    itemId: string,
    discountAmount: number,
  ): Promise<void> =>
    api.patch<void>(`${BASE}/${sessionToken}/items/discount`, {
      itemId,
      discountAmount,
    } satisfies ApplyDiscountRequest),

  decrementItem: (
    sessionToken: string,
    internalCode: string,
  ): Promise<SaleResponse> =>
    api.patch<SaleResponse>(
      `${BASE}/${sessionToken}/items/${encodeURIComponent(internalCode)}/decrement`,
    ),

  associateClient: (
    sessionToken: string,
    clientId: string,
  ): Promise<SaleResponse> =>
    api.put<SaleResponse>(`${BASE}/${sessionToken}/client/${clientId}`),

  applyFidelityDiscount: (sessionToken: string): Promise<SaleResponse> =>
    api.post<SaleResponse>(`${BASE}/${sessionToken}/fidelity-discount`),
};
