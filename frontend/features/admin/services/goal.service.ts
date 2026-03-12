import { api } from "@/shared/services/api";
import type {
  GoalAssessmentResult,
  GoalRequest,
  GoalResponse,
} from "@/features/admin/types";

export const goalService = {
  listAll: (): Promise<GoalResponse[]> => api.get<GoalResponse[]>("/api/goals"),

  create: (data: GoalRequest): Promise<GoalResponse> =>
    api.post<GoalResponse>("/api/goals", data),

  update: (id: string, data: GoalRequest): Promise<GoalResponse> =>
    api.put<GoalResponse>(`/api/goals/${id}`, data),

  activate: (id: string): Promise<GoalResponse> =>
    api.patch<GoalResponse>(`/api/goals/${id}/activate`),

  close: (id: string): Promise<GoalResponse> =>
    api.patch<GoalResponse>(`/api/goals/${id}/close`),

  remove: (id: string): Promise<void> => api.delete<void>(`/api/goals/${id}`),

  assess: (id: string, totalSold: number): Promise<GoalAssessmentResult> =>
    api.get<GoalAssessmentResult>(
      `/api/goals/${id}/assessment?totalSold=${totalSold}`,
    ),

  getProgress: (id: string): Promise<GoalAssessmentResult> =>
    api.get<GoalAssessmentResult>(`/api/goals/${id}/progress`),
};
