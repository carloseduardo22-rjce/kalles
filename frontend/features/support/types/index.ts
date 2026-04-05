export type TicketStatus =
  | "OPEN"
  | "IN_PROGRESS"
  | "WAITING_FOR_CUSTOMER"
  | "RESOLVED"
  | "CLOSED";

export type Priority = "LOW" | "MEDIUM" | "HIGH" | "CRITICAL";

export type InteractionType =
  | "CUSTOMER_MESSAGE"
  | "AGENT_MESSAGE"
  | "INTERNAL_NOTE";

export type AccountRole = "ADMIN" | "OPERATOR";

export interface OpenTicketRequest {
  title: string;
  description: string;
  categoryId: string;
  attachment?: File;
}

export interface UserRequest {
  email: string;
  name: string;
}

export interface CustomerMessageRequest {
  content: string;
}

export interface AgentMessageRequest {
  content: string;
  markAsResolved: boolean;
}

export interface AgentResponse {
  employeeId: string;
  name: string;
}

export interface CategoryResponse {
  id: string;
  name: string;
  subcategory: string;
  defaultPriority: Priority;
  active: boolean;
}

export interface UserResponse {
  id: string;
  email: string;
  name: string;
}

export interface SlaResponse {
  active: boolean;
  startedAt: string | null;
}

export interface InteractionResponse {
  id: string;
  content: string;
  type: InteractionType;
  createdAt: string;
}

export interface TicketResponse {
  id: string;
  title: string;
  description: string;
  status: TicketStatus;
  priority: Priority;
  user: { email: string; name: string };
  agent: AgentResponse | null;
  category: { name: string; subcategory: string; defaultPriority: Priority };
  sla: SlaResponse;
  interactions: InteractionResponse[];
}

export interface AuthMeResponse {
  accountId: string;
  tenantId: string;
  companyId: string | null;
  email: string;
  name: string;
  role: AccountRole;
}
