import { api } from "@/shared/services/api";
import type {
  ProductAdminResponse,
  ProductRequest,
} from "@/features/admin/types";
import type { PaginatedResponse } from "@/shared/types";

const BASE = "/api/products";

export const productAdminService = {
  listAll: (): Promise<ProductAdminResponse[]> =>
    api.get<ProductAdminResponse[]>(`${BASE}?includeInactive=true`),

  listPage: (
    page: number,
    size: number,
  ): Promise<PaginatedResponse<ProductAdminResponse>> =>
    api.get<PaginatedResponse<ProductAdminResponse>>(
      `${BASE}/page?includeInactive=true&page=${page}&size=${size}`,
    ),

  create: (data: ProductRequest): Promise<ProductAdminResponse> =>
    api.post<ProductAdminResponse>(BASE, data),

  update: (id: string, data: ProductRequest): Promise<ProductAdminResponse> =>
    api.put<ProductAdminResponse>(`${BASE}/${id}`, data),

  deactivate: (id: string): Promise<void> => api.delete<void>(`${BASE}/${id}`),
};
