import { api } from "@/shared/services/api";
import type { ProductResponse } from "../types";

export const productService = {
  getAll: (): Promise<ProductResponse[]> =>
    api.get<ProductResponse[]>("/api/products"),

  search: (q: string): Promise<ProductResponse[]> =>
    api.get<ProductResponse[]>(
      `/api/products/search?q=${encodeURIComponent(q)}`,
    ),
};
