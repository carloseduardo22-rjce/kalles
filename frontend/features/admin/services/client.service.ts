import { api } from "@/shared/services/api";
import type { ClientRequest, ClientResponse } from "@/features/admin/types";

const BASE = "/api/clients";

export const clientService = {
  listAll: (): Promise<ClientResponse[]> => api.get<ClientResponse[]>(BASE),

  findById: (id: string): Promise<ClientResponse> =>
    api.get<ClientResponse>(`${BASE}/${id}`),

  create: (data: ClientRequest): Promise<ClientResponse> =>
    api.post<ClientResponse>(BASE, data),

  update: (id: string, data: ClientRequest): Promise<ClientResponse> =>
    api.put<ClientResponse>(`${BASE}/${id}`, data),

  delete: (id: string): Promise<void> => api.delete<void>(`${BASE}/${id}`),
};
