import { api } from "@/shared/services/api";
import type { StockRequest, StockResponse } from "@/features/admin/types";

const BASE = "/api/stocks";

export const stockService = {
  setStock: (data: StockRequest): Promise<StockResponse> =>
    api.post<StockResponse>(BASE, data),

  getByProduct: (productId: string): Promise<StockResponse[]> =>
    api.get<StockResponse[]>(`${BASE}/product/${productId}`),

  getTotalByProduct: (productId: string): Promise<number> =>
    api.get<number>(`${BASE}/product/${productId}/total`),

  getByLocation: (locationId: string): Promise<StockResponse[]> =>
    api.get<StockResponse[]>(`${BASE}/location/${locationId}`),
};
