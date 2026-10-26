export interface ApiErrorShape {
  status: number;
  message: string;
}

export interface PaginatedResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  page: number;
  size: number;
}

export type AccountRole = "ADMIN" | "OPERATOR";

export interface AuthMeResponse {
  accountId: string;
  tenantId: string;
  companyId: string | null;
  email: string;
  name: string;
  role: AccountRole;
}
