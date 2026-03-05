import { api } from "@/shared/services/api";
import type {
  ProductAdminResponse,
  ProductRequest,
} from "@/features/admin/types";

const BASE = "/api/products";

export const productAdminService = {
  listAll: (): Promise<ProductAdminResponse[]> =>
    api.get<ProductAdminResponse[]>(`${BASE}?includeInactive=true`),

  create: (data: ProductRequest): Promise<ProductAdminResponse> =>
    api.post<ProductAdminResponse>(BASE, data),

  update: (id: string, data: ProductRequest): Promise<ProductAdminResponse> =>
    api.put<ProductAdminResponse>(`${BASE}/${id}`, data),

  deactivate: (id: string): Promise<void> => api.delete<void>(`${BASE}/${id}`),
};
