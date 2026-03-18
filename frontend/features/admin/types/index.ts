// ─── Clients ───────────────────────────────────────────────────────────────

export interface ClientRequest {
  name: string;
  birthDate?: string | null;
  gender?: string | null;
  cpf: string;
  codeCountry?: string | null;
  cellphone?: string | null;
  rg?: string | null;
  nameFather?: string | null;
  nameMother?: string | null;
  observations?: string | null;
}

export interface ClientResponse {
  id: string;
  name: string;
  birthDate: string | null;
  gender: string | null;
  cpf: string;
  codeCountry: string | null;
  cellphone: string | null;
  rg: string | null;
  nameFather: string | null;
  nameMother: string | null;
  observations: string | null;
}

// ─── Products (Admin) ──────────────────────────────────────────────────────

export interface ProductRequest {
  name: string;
  internalCode: string;
  barcode?: string | null;
  description?: string | null;
  price: number;
  active: boolean;
}

export interface ProductAdminResponse {
  id: string;
  name: string;
  internalCode: string;
  barcode: string | null;
  description: string | null;
  price: number;
  active: boolean;
  stockQuantity?: number;
  warehouse?: string;
  location?: string;
}

// ─── Operators ─────────────────────────────────────────────────────────────

export type PermissionLevel = "BASIC" | "SUPERVISOR" | "MANAGER" | "ADMIN";

export interface OperatorRequest {
  name: string;
  code: string;
  permissionLevel: PermissionLevel;
}

export interface OperatorAdminResponse {
  id: string;
  name: string;
  code: string;
  permissionLevel: PermissionLevel | null;
}

// ─── Warehouses ────────────────────────────────────────────────────────────

export interface WarehouseRequest {
  name: string;
  address?: string | null;
}

export interface WarehouseResponse {
  id: string;
  name: string;
  address: string | null;
  active: boolean;
}

// ─── Locations ────────────────────────────────────────────────────────────

export interface LocationRequest {
  code: string;
  description?: string | null;
}

export interface LocationResponse {
  id: string;
  warehouseId: string;
  warehouseName: string;
  code: string;
  description: string | null;
}

// ─── Fidelity ─────────────────────────────────────────────────────────────

export interface FidelityPolicyRequest {
  objectivePoints: number;
  configuredDiscount: number;
  valuePoint: number;
}

export interface FidelityPolicyResponse {
  id: string;
  objectivePoints: number;
  configuredDiscount: number;
  valuePoint: number;
  active: boolean;
  createdAt: string;
}

export interface FidelityResponse {
  id: string;
  clientId: string;
  points: number;
  availableDiscount: number;
  createdAt: string;
  expired: boolean;
}

// ─── Goals ───────────────────────────────────────────────────────────────────

export type GoalStatus = "DRAFT" | "ACTIVE" | "CLOSED";
export type Periodicity = "WEEKLY" | "MONTHLY";

export interface GoalRequest {
  targetValue: number;
  periodicity: Periodicity;
  startDate: string;
  endDate: string;
}

export interface GoalResponse {
  id: string;
  targetValue: number;
  periodicity: Periodicity;
  startDate: string;
  endDate: string;
  status: GoalStatus;
}

export interface GoalAssessmentResult {
  achievedValue: number;
  gap: number;
}

// ─── Stock ────────────────────────────────────────────────────────────────

export interface StockRequest {
  productId: string;
  locationId: string;
  quantity: number;
}

export interface StockResponse {
  id: string;
  productId: string;
  productName: string;
  productInternalCode: string;
  locationId: string;
  locationCode: string;
  warehouseName: string;
  quantity: number;
}
