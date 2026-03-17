"use client";

import { useState } from "react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import {
  Package,
  Plus,
  Pencil,
  PowerOff,
  RefreshCw,
  Search,
} from "lucide-react";
import { useForm } from "react-hook-form";
import { toast } from "sonner";

import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
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
import { productAdminService } from "@/features/admin/services/product-admin.service";
import { formatCurrency } from "@/shared/utils/formatters";
import type {
  ProductAdminResponse,
  ProductRequest,
} from "@/features/admin/types";

/* ─── Form ──────────────────────────────────────────────────────────────── */
function ProductForm({
  defaultValues,
  onSubmit,
  isPending,
  onCancel,
}: {
  defaultValues?: Partial<ProductRequest>;
  onSubmit: (data: ProductRequest) => void;
  isPending: boolean;
  onCancel: () => void;
}) {
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<ProductRequest>({
    defaultValues: defaultValues ?? { active: true },
  });

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
      <div className="grid grid-cols-2 gap-4">
        <div className="col-span-2 space-y-1.5">
          <Label htmlFor="name">Nome *</Label>
          <Input
            id="name"
            {...register("name", { required: "Nome é obrigatório" })}
            placeholder="Nome do produto"
          />
          {errors.name && (
            <p className="text-xs text-destructive">{errors.name.message}</p>
          )}
        </div>

        <div className="space-y-1.5">
          <Label htmlFor="internalCode">Código Interno *</Label>
          <Input
            id="internalCode"
            {...register("internalCode", {
              required: "Código interno é obrigatório",
            })}
            placeholder="Ex: PROD-001"
            className="font-mono"
          />
          {errors.internalCode && (
            <p className="text-xs text-destructive">
              {errors.internalCode.message}
            </p>
          )}
        </div>

        <div className="space-y-1.5">
          <Label htmlFor="barcode">Código de Barras</Label>
          <Input
            id="barcode"
            {...register("barcode")}
            placeholder="EAN-13 ou outro"
            className="font-mono"
          />
        </div>

        <div className="space-y-1.5">
          <Label htmlFor="price">Preço *</Label>
          <Input
            id="price"
            type="number"
            step="0.01"
            min="0.01"
            {...register("price", {
              required: "Preço é obrigatório",
              min: { value: 0.01, message: "Preço deve ser maior que zero" },
              valueAsNumber: true,
            })}
            placeholder="0,00"
          />
          {errors.price && (
            <p className="text-xs text-destructive">{errors.price.message}</p>
          )}
        </div>

        <div className="col-span-2 space-y-1.5">
          <Label htmlFor="description">Descrição</Label>
          <Textarea
            id="description"
            {...register("description")}
            placeholder="Descrição detalhada do produto…"
            rows={3}
          />
        </div>
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

/* ─── Page ──────────────────────────────────────────────────────────────── */
export default function AdminProdutosPage() {
  const queryClient = useQueryClient();
  const [search, setSearch] = useState("");
  const [createOpen, setCreateOpen] = useState(false);
  const [editTarget, setEditTarget] = useState<ProductAdminResponse | null>(
    null,
  );
  const [deactivateTarget, setDeactivateTarget] =
    useState<ProductAdminResponse | null>(null);

  const {
    data: products = [],
    isLoading,
    error,
    refetch,
    isFetching,
  } = useQuery({
    queryKey: ["admin-produtos"],
    queryFn: () => productAdminService.listAll(),
  });

  const createMutation = useMutation({
    mutationFn: (data: ProductRequest) => productAdminService.create(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["admin-produtos"] });
      queryClient.invalidateQueries({ queryKey: ["products-all"] });
      setCreateOpen(false);
      toast.success("Produto cadastrado com sucesso.");
    },
    onError: (err: unknown) => {
      toast.error(
        err instanceof Error ? err.message : "Erro ao cadastrar produto.",
      );
    },
  });

  const updateMutation = useMutation({
    mutationFn: ({ id, data }: { id: string; data: ProductRequest }) =>
      productAdminService.update(id, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["admin-produtos"] });
      queryClient.invalidateQueries({ queryKey: ["products-all"] });
      setEditTarget(null);
      toast.success("Produto atualizado com sucesso.");
    },
    onError: (err: unknown) => {
      toast.error(
        err instanceof Error ? err.message : "Erro ao atualizar produto.",
      );
    },
  });

  const deactivateMutation = useMutation({
    mutationFn: (id: string) => productAdminService.deactivate(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["admin-produtos"] });
      queryClient.invalidateQueries({ queryKey: ["products-all"] });
      setDeactivateTarget(null);
      toast.success("Produto desativado.");
    },
    onError: (err: unknown) => {
      toast.error(
        err instanceof Error ? err.message : "Erro ao desativar produto.",
      );
    },
  });

  const filtered = search.trim()
    ? products.filter((p) => {
        const q = search.toLowerCase();
        return (
          p.name.toLowerCase().includes(q) ||
          p.internalCode.toLowerCase().includes(q) ||
          (p.barcode ?? "").toLowerCase().includes(q)
        );
      })
    : products;

  return (
    <div className="flex h-full flex-col overflow-hidden">
      {/* ─── Header ─── */}
      <header className="flex items-center gap-3 border-b bg-card px-4 py-3 shadow-sm">
        <Package className="h-5 w-5 text-primary" />
        <div>
          <h1 className="text-sm font-semibold leading-none">
            Cadastro de Produtos
          </h1>
          <p className="mt-0.5 text-xs text-muted-foreground">
            {isLoading ? "Carregando…" : `${products.length} produto(s)`}
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
          <Button size="sm" onClick={() => setCreateOpen(true)}>
            <Plus className="mr-1.5 h-4 w-4" />
            Novo Produto
          </Button>
        </div>
      </header>

      {/* ─── Search ─── */}
      <div className="border-b bg-muted/30 px-4 py-3">
        <div className="relative max-w-xl">
          <Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
          <Input
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            placeholder="Buscar por nome, código interno ou código de barras…"
            className="bg-background pl-9"
          />
        </div>
      </div>

      {/* ─── Content ─── */}
      <div className="flex-1 overflow-auto">
        {isLoading ? (
          <div className="flex h-40 items-center justify-center">
            <LoadingSpinner size="lg" label="Carregando produtos…" />
          </div>
        ) : error ? (
          <ErrorAlert error={error} title="Erro ao carregar produtos" />
        ) : filtered.length === 0 ? (
          <div className="flex h-40 flex-col items-center justify-center gap-2 text-muted-foreground">
            <Package className="h-10 w-10 opacity-20" />
            <p className="text-sm font-medium">
              {search
                ? "Nenhum produto encontrado"
                : "Nenhum produto cadastrado"}
            </p>
          </div>
        ) : (
          <table className="w-full text-sm">
            <thead className="border-b bg-muted/50">
              <tr>
                <th className="w-28 px-4 py-3 text-left text-xs font-semibold uppercase tracking-wide text-muted-foreground">
                  Cód. Interno
                </th>
                <th className="w-36 px-4 py-3 text-left text-xs font-semibold uppercase tracking-wide text-muted-foreground">
                  Cód. Barras
                </th>
                <th className="px-4 py-3 text-left text-xs font-semibold uppercase tracking-wide text-muted-foreground">
                  Nome
                </th>
                <th className="w-24 px-4 py-3 text-center text-xs font-semibold uppercase tracking-wide text-muted-foreground">
                  Qtd
                </th>
                <th className="w-32 px-4 py-3 text-left text-xs font-semibold uppercase tracking-wide text-muted-foreground">
                  Estoque / Local
                </th>
                <th className="w-28 px-4 py-3 text-right text-xs font-semibold uppercase tracking-wide text-muted-foreground">
                  Preço
                </th>
                <th className="w-20 px-4 py-3 text-center text-xs font-semibold uppercase tracking-wide text-muted-foreground">
                  Status
                </th>
                <th className="w-20 px-4 py-3" />
              </tr>
            </thead>
            <tbody>
              {filtered.map((product, i) => (
                <tr
                  key={product.id}
                  className={`border-t transition-colors hover:bg-accent ${i % 2 !== 0 ? "bg-muted/20" : ""}`}
                >
                  <td className="px-4 py-3 font-mono text-xs text-muted-foreground">
                    {product.internalCode}
                  </td>
                  <td className="px-4 py-3 font-mono text-xs text-muted-foreground">
                    {product.barcode ?? <span className="opacity-40">—</span>}
                  </td>
                  <td className="px-4 py-3 font-medium">{product.name}</td>
                  <td className="px-4 py-3 text-center font-medium">
                    {product.stockQuantity ?? (
                      <span className="opacity-40">0</span>
                    )}
                  </td>
                  <td className="px-4 py-3 text-xs text-muted-foreground">
                    {product.warehouse ? (
                      <span className="inline-flex flex-col">
                        <span className="font-semibold text-foreground">
                          {product.warehouse}
                        </span>
                        <span>{product.location}</span>
                      </span>
                    ) : (
                      <span className="opacity-40">—</span>
                    )}
                  </td>
                  <td className="px-4 py-3 text-right tabular-nums font-semibold">
                    {formatCurrency(product.price)}
                  </td>
                  <td className="px-4 py-3 text-center">
                    <Badge
                      variant={product.active ? "outline" : "secondary"}
                      className={
                        product.active
                          ? "border-green-300 text-green-700 dark:border-green-800 dark:text-green-400"
                          : ""
                      }
                    >
                      {product.active ? "Ativo" : "Inativo"}
                    </Badge>
                  </td>
                  <td className="px-4 py-3">
                    <div className="flex items-center justify-end gap-1">
                      <Button
                        variant="ghost"
                        size="icon"
                        className="h-7 w-7"
                        onClick={() => setEditTarget(product)}
                        title="Editar"
                      >
                        <Pencil className="h-3.5 w-3.5" />
                      </Button>
                      {product.active && (
                        <Button
                          variant="ghost"
                          size="icon"
                          className="h-7 w-7 text-destructive hover:text-destructive"
                          onClick={() => setDeactivateTarget(product)}
                          title="Desativar"
                        >
                          <PowerOff className="h-3.5 w-3.5" />
                        </Button>
                      )}
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>

      {/* ─── Create Dialog ─── */}
      <Dialog open={createOpen} onOpenChange={setCreateOpen}>
        <DialogContent className="max-w-lg">
          <DialogHeader>
            <DialogTitle>Novo Produto</DialogTitle>
            <DialogDescription>
              Campos marcados com * são obrigatórios.
            </DialogDescription>
          </DialogHeader>
          <ProductForm
            onSubmit={(data) =>
              createMutation.mutate({ ...data, active: true })
            }
            isPending={createMutation.isPending}
            onCancel={() => setCreateOpen(false)}
          />
        </DialogContent>
      </Dialog>

      {/* ─── Edit Dialog ─── */}
      <Dialog
        open={!!editTarget}
        onOpenChange={(o) => !o && setEditTarget(null)}
      >
        <DialogContent className="max-w-lg">
          <DialogHeader>
            <DialogTitle>Editar Produto</DialogTitle>
            <DialogDescription>Atualize os dados do produto.</DialogDescription>
          </DialogHeader>
          {editTarget && (
            <ProductForm
              defaultValues={editTarget}
              onSubmit={(data) =>
                updateMutation.mutate({
                  id: editTarget.id,
                  data: { ...data, active: editTarget.active },
                })
              }
              isPending={updateMutation.isPending}
              onCancel={() => setEditTarget(null)}
            />
          )}
        </DialogContent>
      </Dialog>

      {/* ─── Deactivate Dialog ─── */}
      <AlertDialog
        open={!!deactivateTarget}
        onOpenChange={(o) => !o && setDeactivateTarget(null)}
      >
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>Desativar produto?</AlertDialogTitle>
            <AlertDialogDescription>
              O produto <strong>{deactivateTarget?.name}</strong> será marcado
              como inativo e não aparecerá mais nas buscas do PDV.
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel disabled={deactivateMutation.isPending}>
              Cancelar
            </AlertDialogCancel>
            <AlertDialogAction
              className="bg-destructive text-destructive-foreground hover:bg-destructive/90"
              disabled={deactivateMutation.isPending}
              onClick={() =>
                deactivateTarget &&
                deactivateMutation.mutate(deactivateTarget.id)
              }
            >
              {deactivateMutation.isPending ? (
                <LoadingSpinner size="sm" />
              ) : (
                "Desativar"
              )}
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </div>
  );
}
