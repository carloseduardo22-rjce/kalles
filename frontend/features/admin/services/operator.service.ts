import { api } from "@/shared/services/api";
import type {
  OperatorAdminResponse,
  OperatorRequest,
} from "@/features/admin/types";

const BASE = "/api/operators";

export const operatorService = {
  listAll: (): Promise<OperatorAdminResponse[]> =>
    api.get<OperatorAdminResponse[]>(BASE),

  create: (data: OperatorRequest): Promise<OperatorAdminResponse> =>
    api.post<OperatorAdminResponse>(BASE, data),

  update: (id: string, data: OperatorRequest): Promise<OperatorAdminResponse> =>
    api.put<OperatorAdminResponse>(`${BASE}/${id}`, data),

  deactivate: (id: string): Promise<void> => api.delete<void>(`${BASE}/${id}`),
};
