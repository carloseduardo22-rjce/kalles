"use client";

import { useState, Fragment } from "react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import {
  Warehouse,
  Plus,
  Pencil,
  ChevronDown,
  ChevronRight,
  MapPin,
  Trash2,
  RefreshCw,
} from "lucide-react";
import { useForm } from "react-hook-form";
import { toast } from "sonner";

import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Badge } from "@/components/ui/badge";
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogFooter,
  DialogDescription,
} from "@/components/ui/dialog";
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
} from "@/components/ui/alert-dialog";
import { LoadingSpinner } from "@/shared/components/loading-spinner";
import { ErrorAlert } from "@/shared/components/error-alert";
import { warehouseService } from "@/features/admin/services/warehouse.service";
import type {
  LocationRequest,
  LocationResponse,
  WarehouseRequest,
  WarehouseResponse,
} from "@/features/admin/types";
import {
  normalizeLocationRequest,
  normalizeWarehouseRequest,
} from "@/features/admin/utils/form-normalization";

/* ─── Warehouse Form ────────────────────────────────────────────────────── */
function WarehouseForm({
  defaultValues,
  onSubmit,
  isPending,
  onCancel,
}: {
  defaultValues?: Partial<WarehouseRequest>;
  onSubmit: (data: WarehouseRequest) => void;
  isPending: boolean;
  onCancel: () => void;
}) {
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<WarehouseRequest>({
    defaultValues: defaultValues ?? {},
  });

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
      <div className="space-y-1.5">
        <Label htmlFor="wh-name">Nome *</Label>
        <Input
          id="wh-name"
          {...register("name", { required: "Nome é obrigatório" })}
          placeholder="Ex: Depósito Central"
        />
        {errors.name && (
          <p className="text-xs text-destructive">{errors.name.message}</p>
        )}
      </div>
      <div className="space-y-1.5">
        <Label htmlFor="wh-address">Endereço</Label>
        <Input
          id="wh-address"
          {...register("address")}
          placeholder="Endereço do depósito"
        />
      </div>
      <DialogFooter>
        <Button
          type="button"
          variant="outline"
          onClick={onCancel}
          disabled={isPending}
        >
          Cancelar
        </Button>
        <Button type="submit" disabled={isPending}>
          {isPending ? <LoadingSpinner size="sm" /> : "Salvar"}
        </Button>
      </DialogFooter>
    </form>
  );
}

/* ─── Location Form ─────────────────────────────────────────────────────── */
function LocationForm({
  defaultValues,
  onSubmit,
  isPending,
  onCancel,
}: {
  defaultValues?: Partial<LocationRequest>;
  onSubmit: (data: LocationRequest) => void;
  isPending: boolean;
  onCancel: () => void;
}) {
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<LocationRequest>({
    defaultValues: defaultValues ?? {},
  });

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
      <div className="space-y-1.5">
        <Label htmlFor="loc-code">Código *</Label>
        <Input
          id="loc-code"
          {...register("code", { required: "Código é obrigatório" })}
          placeholder="Ex: A-01, PRATELEIRA-3"
          className="font-mono"
        />
        {errors.code && (
          <p className="text-xs text-destructive">{errors.code.message}</p>
        )}
      </div>
      <div className="space-y-1.5">
        <Label htmlFor="loc-desc">Descrição</Label>
        <Input
          id="loc-desc"
          {...register("description")}
          placeholder="Ex: Corredor A, Lado Esquerdo"
        />
      </div>
      <DialogFooter>
        <Button
          type="button"
          variant="outline"
          onClick={onCancel}
          disabled={isPending}
        >
          Cancelar
        </Button>
        <Button type="submit" disabled={isPending}>
          {isPending ? <LoadingSpinner size="sm" /> : "Salvar"}
        </Button>
      </DialogFooter>
    </form>
  );
}

/* ─── Expanded Locations Row ────────────────────────────────────────────── */
function LocationsPanel({
  warehouse,
  onAdd,
  onEdit,
  onDelete,
}: {
  warehouse: WarehouseResponse;
  onAdd: () => void;
  onEdit: (loc: LocationResponse) => void;
  onDelete: (loc: LocationResponse) => void;
}) {
  const { data: locations = [], isLoading } = useQuery({
    queryKey: ["admin-locations", warehouse.id],
    queryFn: () => warehouseService.listLocations(warehouse.id),
  });

  return (
    <tr>
      <td colSpan={4} className="border-t bg-muted/40 px-6 pb-3 pt-2">
        <div className="flex items-center justify-between pb-2">
          <span className="text-xs font-semibold uppercase tracking-wide text-muted-foreground">
            Localizações ({locations.length})
          </span>
          <Button
            size="sm"
            variant="outline"
            className="h-7 text-xs"
            onClick={onAdd}
          >
            <Plus className="mr-1 h-3 w-3" />
            Nova Localização
          </Button>
        </div>
        {isLoading ? (
          <LoadingSpinner size="sm" label="Carregando…" />
        ) : locations.length === 0 ? (
          <p className="text-xs text-muted-foreground">
            Nenhuma localização cadastrada.
          </p>
        ) : (
          <div className="space-y-1">
            {locations.map((loc) => (
              <div
                key={loc.id}
                className="flex items-center justify-between rounded-md border bg-card px-3 py-1.5 text-xs"
              >
                <div className="flex items-center gap-2">
                  <MapPin className="h-3 w-3 text-muted-foreground" />
                  <span className="font-mono font-medium">{loc.code}</span>
                  {loc.description && (
                    <span className="text-muted-foreground">
                      — {loc.description}
                    </span>
                  )}
                </div>
                <div className="flex gap-1">
                  <Button
                    variant="ghost"
                    size="icon"
                    className="h-6 w-6"
                    onClick={() => onEdit(loc)}
                    title="Editar"
                  >
                    <Pencil className="h-3 w-3" />
                  </Button>
                  <Button
                    variant="ghost"
                    size="icon"
                    className="h-6 w-6 text-destructive hover:text-destructive"
                    onClick={() => onDelete(loc)}
                    title="Excluir"
                  >
                    <Trash2 className="h-3 w-3" />
                  </Button>
                </div>
              </div>
            ))}
          </div>
        )}
      </td>
    </tr>
  );
}

/* ─── Page ──────────────────────────────────────────────────────────────── */
export default function DepositosPage() {
  const queryClient = useQueryClient();
  const [expandedId, setExpandedId] = useState<string | null>(null);
  const [createWhOpen, setCreateWhOpen] = useState(false);
  const [editWh, setEditWh] = useState<WarehouseResponse | null>(null);
  const [deactivateWh, setDeactivateWh] = useState<WarehouseResponse | null>(
    null,
  );
  const [addLocFor, setAddLocFor] = useState<WarehouseResponse | null>(null);
  const [editLoc, setEditLoc] = useState<LocationResponse | null>(null);
  const [deleteLoc, setDeleteLoc] = useState<LocationResponse | null>(null);

  const {
    data: warehouses = [],
    isLoading,
    error,
    refetch,
    isFetching,
  } = useQuery({
    queryKey: ["admin-depositos"],
    queryFn: () => warehouseService.listAll(),
  });

  const createWhMutation = useMutation({
    mutationFn: (data: WarehouseRequest) => warehouseService.create(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["admin-depositos"] });
      setCreateWhOpen(false);
      toast.success("Depósito criado com sucesso.");
    },
    onError: (err: unknown) => {
      toast.error(
        err instanceof Error ? err.message : "Erro ao criar depósito.",
      );
    },
  });

  const updateWhMutation = useMutation({
    mutationFn: ({ id, data }: { id: string; data: WarehouseRequest }) =>
      warehouseService.update(id, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["admin-depositos"] });
      setEditWh(null);
      toast.success("Depósito atualizado.");
    },
    onError: (err: unknown) => {
      toast.error(
        err instanceof Error ? err.message : "Erro ao atualizar depósito.",
      );
    },
  });

  const deactivateWhMutation = useMutation({
    mutationFn: (id: string) => warehouseService.deactivate(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["admin-depositos"] });
      setDeactivateWh(null);
      toast.success("Depósito desativado.");
    },
    onError: (err: unknown) => {
      toast.error(
        err instanceof Error ? err.message : "Erro ao desativar depósito.",
      );
    },
  });

  const createLocMutation = useMutation({
    mutationFn: ({
      warehouseId,
      data,
    }: {
      warehouseId: string;
      data: LocationRequest;
    }) => warehouseService.createLocation(warehouseId, data),
    onSuccess: (_, { warehouseId }) => {
      queryClient.invalidateQueries({
        queryKey: ["admin-locations", warehouseId],
      });
      setAddLocFor(null);
      toast.success("Localização criada.");
    },
    onError: (err: unknown) => {
      toast.error(
        err instanceof Error ? err.message : "Erro ao criar localização.",
      );
    },
  });

  const updateLocMutation = useMutation({
    mutationFn: ({ id, data }: { id: string; data: LocationRequest }) =>
      warehouseService.updateLocation(id, data),
    onSuccess: (result) => {
      queryClient.invalidateQueries({
        queryKey: ["admin-locations", result.warehouseId],
      });
      setEditLoc(null);
      toast.success("Localização atualizada.");
    },
    onError: (err: unknown) => {
      toast.error(
        err instanceof Error ? err.message : "Erro ao atualizar localização.",
      );
    },
  });

  const deleteLocMutation = useMutation({
    mutationFn: (loc: LocationResponse) =>
      warehouseService.deleteLocation(loc.id),
    onSuccess: (_, loc) => {
      queryClient.invalidateQueries({
        queryKey: ["admin-locations", loc.warehouseId],
      });
      setDeleteLoc(null);
      toast.success("Localização removida.");
    },
    onError: (err: unknown) => {
      toast.error(
        err instanceof Error ? err.message : "Erro ao remover localização.",
      );
    },
  });

  return (
    <div className="flex h-full flex-col overflow-hidden">
      {/* ─── Header ─── */}
      <header className="flex items-center gap-3 border-b bg-card px-4 py-3 shadow-sm">
        <Warehouse className="h-5 w-5 text-primary" />
        <div>
          <h1 className="text-sm font-semibold leading-none">
            Depósitos e Localizações
          </h1>
          <p className="mt-0.5 text-xs text-muted-foreground">
            {isLoading
              ? "Carregando…"
              : `${warehouses.length} depósito(s) ativo(s)`}
          </p>
        </div>
        <div className="ml-auto flex items-center gap-2">
          <Button
            variant="ghost"
            size="icon"
            onClick={() => refetch()}
            disabled={isFetching}
            title="Atualizar"
          >
            <RefreshCw
              className={`h-4 w-4 ${isFetching ? "animate-spin" : ""}`}
            />
          </Button>
          <Button size="sm" onClick={() => setCreateWhOpen(true)}>
            <Plus className="mr-1.5 h-4 w-4" />
            Novo Depósito
          </Button>
        </div>
      </header>

      {/* ─── Content ─── */}
      <div className="flex-1 overflow-auto">
        {isLoading ? (
          <div className="flex h-40 items-center justify-center">
            <LoadingSpinner size="lg" label="Carregando depósitos…" />
          </div>
        ) : error ? (
          <ErrorAlert error={error} title="Erro ao carregar depósitos" />
        ) : warehouses.length === 0 ? (
          <div className="flex h-40 flex-col items-center justify-center gap-2 text-muted-foreground">
            <Warehouse className="h-10 w-10 opacity-20" />
            <p className="text-sm font-medium">Nenhum depósito cadastrado</p>
          </div>
        ) : (
          <table className="w-full text-sm">
            <thead className="border-b bg-muted/50">
              <tr>
                <th className="w-8 px-3 py-3" />
                <th className="px-4 py-3 text-left text-xs font-semibold uppercase tracking-wide text-muted-foreground">
                  Nome
                </th>
                <th className="px-4 py-3 text-left text-xs font-semibold uppercase tracking-wide text-muted-foreground">
                  Endereço
                </th>
                <th className="w-28 px-4 py-3" />
              </tr>
            </thead>
            <tbody>
              {warehouses.map((wh, i) => (
                <Fragment key={wh.id}>
                  <tr
                    className={`border-t transition-colors hover:bg-accent ${i % 2 !== 0 ? "bg-muted/20" : ""}`}
                  >
                    <td className="px-3 py-3">
                      <button
                        onClick={() =>
                          setExpandedId(expandedId === wh.id ? null : wh.id)
                        }
                        className="flex h-5 w-5 items-center justify-center rounded text-muted-foreground hover:text-foreground"
                        title={
                          expandedId === wh.id ? "Recolher" : "Ver localizações"
                        }
                      >
                        {expandedId === wh.id ? (
                          <ChevronDown className="h-4 w-4" />
                        ) : (
                          <ChevronRight className="h-4 w-4" />
                        )}
                      </button>
                    </td>
                    <td className="px-4 py-3 font-medium">
                      {wh.name}
                      <Badge
                        variant="outline"
                        className="ml-2 border-green-300 text-[10px] text-green-700 dark:border-green-800 dark:text-green-400"
                      >
                        Ativo
                      </Badge>
                    </td>
                    <td className="px-4 py-3 text-sm text-muted-foreground">
                      {wh.address ?? <span className="opacity-40">—</span>}
                    </td>
                    <td className="px-4 py-3">
                      <div className="flex items-center justify-end gap-1">
                        <Button
                          variant="ghost"
                          size="icon"
                          className="h-7 w-7"
                          onClick={() => setEditWh(wh)}
                          title="Editar"
                        >
                          <Pencil className="h-3.5 w-3.5" />
                        </Button>
                        <Button
                          variant="ghost"
                          size="icon"
                          className="h-7 w-7 text-destructive hover:text-destructive"
                          onClick={() => setDeactivateWh(wh)}
                          title="Desativar"
                        >
                          <Trash2 className="h-3.5 w-3.5" />
                        </Button>
                      </div>
                    </td>
                  </tr>
                  {expandedId === wh.id && (
                    <LocationsPanel
                      key={`loc-${wh.id}`}
                      warehouse={wh}
                      onAdd={() => setAddLocFor(wh)}
                      onEdit={(loc) => setEditLoc(loc)}
                      onDelete={(loc) => setDeleteLoc(loc)}
                    />
                  )}
                </Fragment>
              ))}
            </tbody>
          </table>
        )}
      </div>

      {/* ─── Create Warehouse Dialog ─── */}
      <Dialog open={createWhOpen} onOpenChange={setCreateWhOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Novo Depósito</DialogTitle>
            <DialogDescription>
              Cadastre um novo depósito de armazenamento.
            </DialogDescription>
          </DialogHeader>
          <WarehouseForm
            onSubmit={(data) =>
              createWhMutation.mutate(normalizeWarehouseRequest(data))
            }
            isPending={createWhMutation.isPending}
            onCancel={() => setCreateWhOpen(false)}
          />
        </DialogContent>
      </Dialog>

      {/* ─── Edit Warehouse Dialog ─── */}
      <Dialog open={!!editWh} onOpenChange={(o) => !o && setEditWh(null)}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Editar Depósito</DialogTitle>
            <DialogDescription>
              Atualize os dados do depósito.
            </DialogDescription>
          </DialogHeader>
          {editWh && (
            <WarehouseForm
              defaultValues={editWh}
              onSubmit={(data) =>
                updateWhMutation.mutate({
                  id: editWh.id,
                  data: normalizeWarehouseRequest(data),
                })
              }
              isPending={updateWhMutation.isPending}
              onCancel={() => setEditWh(null)}
            />
          )}
        </DialogContent>
      </Dialog>

      {/* ─── Deactivate Warehouse Dialog ─── */}
      <AlertDialog
        open={!!deactivateWh}
        onOpenChange={(o) => !o && setDeactivateWh(null)}
      >
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>Desativar depósito?</AlertDialogTitle>
            <AlertDialogDescription>
              O depósito <strong>{deactivateWh?.name}</strong> será desativado.
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel>Cancelar</AlertDialogCancel>
            <AlertDialogAction
              className="bg-destructive text-destructive-foreground hover:bg-destructive/90"
              disabled={deactivateWhMutation.isPending}
              onClick={() =>
                deactivateWh && deactivateWhMutation.mutate(deactivateWh.id)
              }
            >
              {deactivateWhMutation.isPending ? (
                <LoadingSpinner size="sm" />
              ) : (
                "Desativar"
              )}
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>

      {/* ─── Add Location Dialog ─── */}
      <Dialog open={!!addLocFor} onOpenChange={(o) => !o && setAddLocFor(null)}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Nova Localização</DialogTitle>
            <DialogDescription>
              Adicionar localização em <strong>{addLocFor?.name}</strong>.
            </DialogDescription>
          </DialogHeader>
          {addLocFor && (
            <LocationForm
              onSubmit={(data) =>
                createLocMutation.mutate({
                  warehouseId: addLocFor.id,
                  data: normalizeLocationRequest(data),
                })
              }
              isPending={createLocMutation.isPending}
              onCancel={() => setAddLocFor(null)}
            />
          )}
        </DialogContent>
      </Dialog>

      {/* ─── Edit Location Dialog ─── */}
      <Dialog open={!!editLoc} onOpenChange={(o) => !o && setEditLoc(null)}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Editar Localização</DialogTitle>
            <DialogDescription>
              Atualize os dados da localização.
            </DialogDescription>
          </DialogHeader>
          {editLoc && (
            <LocationForm
              defaultValues={editLoc}
              onSubmit={(data) =>
                updateLocMutation.mutate({
                  id: editLoc.id,
                  data: normalizeLocationRequest(data),
                })
              }
              isPending={updateLocMutation.isPending}
              onCancel={() => setEditLoc(null)}
            />
          )}
        </DialogContent>
      </Dialog>

      {/* ─── Delete Location Dialog ─── */}
      <AlertDialog
        open={!!deleteLoc}
        onOpenChange={(o) => !o && setDeleteLoc(null)}
      >
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>Remover localização?</AlertDialogTitle>
            <AlertDialogDescription>
              A localização <strong>{deleteLoc?.code}</strong> será removida
              permanentemente.
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel>Cancelar</AlertDialogCancel>
            <AlertDialogAction
              className="bg-destructive text-destructive-foreground hover:bg-destructive/90"
              disabled={deleteLocMutation.isPending}
              onClick={() => deleteLoc && deleteLocMutation.mutate(deleteLoc)}
            >
              {deleteLocMutation.isPending ? (
                <LoadingSpinner size="sm" />
              ) : (
                "Remover"
              )}
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </div>
  );
}
