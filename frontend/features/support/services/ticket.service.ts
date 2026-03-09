import { api } from "@/shared/services/api";
import type {
  OpenTicketRequest,
  TicketResponse,
  TicketStatus,
} from "@/features/support/types";

const BASE = "/api/tickets";

export const ticketService = {
  listAll: (status?: TicketStatus): Promise<TicketResponse[]> => {
    const path = status ? `${BASE}?status=${status}` : BASE;
    return api.get<TicketResponse[]>(path);
  },

  findById: (id: string): Promise<TicketResponse> =>
    api.get<TicketResponse>(`${BASE}/${id}`),

  listByUser: (userId: string): Promise<TicketResponse[]> =>
    api.get<TicketResponse[]>(`${BASE}/user/${userId}`),

  open: (data: OpenTicketRequest): Promise<TicketResponse> =>
    api.post<TicketResponse>(BASE, data),
};
