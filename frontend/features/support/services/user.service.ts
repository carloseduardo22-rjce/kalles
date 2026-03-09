import { api } from "@/shared/services/api";
import type { UserRequest, UserResponse } from "@/features/support/types";

const BASE = "/api/users";

export const supportUserService = {
  listAll: (): Promise<UserResponse[]> => api.get<UserResponse[]>(BASE),

  findById: (id: string): Promise<UserResponse> =>
    api.get<UserResponse>(`${BASE}/${id}`),

  create: (data: UserRequest): Promise<UserResponse> =>
    api.post<UserResponse>(BASE, data),
};
