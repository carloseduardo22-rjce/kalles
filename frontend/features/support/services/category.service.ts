import { api } from "@/shared/services/api";
import type { CategoryResponse } from "@/features/support/types";

const BASE = "/api/categories";

export const categoryService = {
  listAll: (): Promise<CategoryResponse[]> => api.get<CategoryResponse[]>(BASE),

  findById: (id: string): Promise<CategoryResponse> =>
    api.get<CategoryResponse>(`${BASE}/${id}`),
};
