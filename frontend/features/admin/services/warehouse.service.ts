import { api } from "@/shared/services/api";
import type {
  LocationRequest,
  LocationResponse,
  WarehouseRequest,
  WarehouseResponse,
} from "@/features/admin/types";

const WAREHOUSES = "/api/warehouses";

export const warehouseService = {
  listAll: (): Promise<WarehouseResponse[]> =>
    api.get<WarehouseResponse[]>(WAREHOUSES),

  findById: (id: string): Promise<WarehouseResponse> =>
    api.get<WarehouseResponse>(`${WAREHOUSES}/${id}`),

  create: (data: WarehouseRequest): Promise<WarehouseResponse> =>
    api.post<WarehouseResponse>(WAREHOUSES, data),

  update: (id: string, data: WarehouseRequest): Promise<WarehouseResponse> =>
    api.put<WarehouseResponse>(`${WAREHOUSES}/${id}`, data),

  deactivate: (id: string): Promise<void> =>
    api.delete<void>(`${WAREHOUSES}/${id}`),

  // ─── Locations ─────────────────────────────────────────────────────────

  listLocations: (warehouseId: string): Promise<LocationResponse[]> =>
    api.get<LocationResponse[]>(`${WAREHOUSES}/${warehouseId}/locations`),

  createLocation: (
    warehouseId: string,
    data: LocationRequest,
  ): Promise<LocationResponse> =>
    api.post<LocationResponse>(`${WAREHOUSES}/${warehouseId}/locations`, data),

  updateLocation: (
    locationId: string,
    data: LocationRequest,
  ): Promise<LocationResponse> =>
    api.put<LocationResponse>(`${WAREHOUSES}/locations/${locationId}`, data),

  deleteLocation: (locationId: string): Promise<void> =>
    api.delete<void>(`${WAREHOUSES}/locations/${locationId}`),

  getLocation: (locationId: string): Promise<LocationResponse> =>
    api.get<LocationResponse>(`${WAREHOUSES}/locations/${locationId}`),
};
