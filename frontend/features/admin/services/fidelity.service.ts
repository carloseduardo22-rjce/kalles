import { api } from "@/shared/services/api";
import type {
  FidelityPolicyRequest,
  FidelityPolicyResponse,
  FidelityResponse,
} from "@/features/admin/types";

export const fidelityService = {
  // ─── Policies ────────────────────────────────────────────────────────────

  createPolicy: (
    data: FidelityPolicyRequest,
  ): Promise<FidelityPolicyResponse> =>
    api.post<FidelityPolicyResponse>("/api/fidelity-policies", data),

  getActivePolicy: (): Promise<FidelityPolicyResponse> =>
    api.get<FidelityPolicyResponse>("/api/fidelity-policies/active"),

  listPolicies: (): Promise<FidelityPolicyResponse[]> =>
    api.get<FidelityPolicyResponse[]>("/api/fidelity-policies"),

  // ─── Client fidelity ─────────────────────────────────────────────────────

  getByClientId: (clientId: string): Promise<FidelityResponse> =>
    api.get<FidelityResponse>(`/api/fidelity/client/${clientId}`),

  enroll: (clientId: string): Promise<FidelityResponse> =>
    api.post<FidelityResponse>(`/api/fidelity/enroll/${clientId}`),
};
