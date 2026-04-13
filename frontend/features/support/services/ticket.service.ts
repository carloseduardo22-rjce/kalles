import { api } from "@/shared/services/api";
import type {
  AgentMessageRequest,
  AuthMeResponse,
  CustomerMessageRequest,
  OpenTicketRequest,
  TicketResponse,
  TicketStatus,
} from "@/features/support/types";
import type { PaginatedResponse } from "@/shared/types";

const BASE = "/api/tickets";

export const ticketService = {
  listAll: (status?: TicketStatus): Promise<TicketResponse[]> => {
    const path = status ? `${BASE}?status=${status}` : BASE;
    return api.get<TicketResponse[]>(path);
  },

  listPage: (
    page: number,
    size: number,
    status?: TicketStatus,
  ): Promise<PaginatedResponse<TicketResponse>> => {
    const params = new URLSearchParams({
      page: String(page),
      size: String(size),
    });
    if (status) {
      params.set("status", status);
    }
    return api.get<PaginatedResponse<TicketResponse>>(`${BASE}/page?${params.toString()}`);
  },

  findById: (id: string): Promise<TicketResponse> =>
    api.get<TicketResponse>(`${BASE}/${id}`),

  open: (data: OpenTicketRequest): Promise<TicketResponse> =>
    api.post<TicketResponse>(BASE, {
      title: data.title,
      description: data.description,
      categoryId: data.categoryId,
    }),

  sendCustomerMessage: (
    id: string,
    data: CustomerMessageRequest,
  ): Promise<TicketResponse> => api.post<TicketResponse>(`${BASE}/${id}/customer-message`, data),

  editCustomerMessage: (
    id: string,
    data: CustomerMessageRequest,
  ): Promise<TicketResponse> => api.patch<TicketResponse>(`${BASE}/${id}/customer-message`, data),

  sendAgentMessage: (
    id: string,
    data: AgentMessageRequest,
  ): Promise<TicketResponse> => api.post<TicketResponse>(`${BASE}/${id}/agent-message`, data),

  editAgentMessage: (
    id: string,
    data: CustomerMessageRequest,
  ): Promise<TicketResponse> => api.patch<TicketResponse>(`${BASE}/${id}/agent-message`, data),

  close: (id: string): Promise<TicketResponse> =>
    api.patch<TicketResponse>(`${BASE}/${id}/close`),

  me: (): Promise<AuthMeResponse> => api.get<AuthMeResponse>("/api/auth/me"),
};
