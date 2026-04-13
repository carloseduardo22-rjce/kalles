"use client";

import { useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
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
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
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
import { stockService } from "@/features/admin/services/stock.service";
import { formatCurrency } from "@/shared/utils/formatters";
import type {
  ProductAdminResponse,
  ProductRequest,
} from "@/features/admin/types";
import { normalizeProductRequest } from "@/features/admin/utils/form-normalization";
import { useCompany } from "@/shared/contexts/company-context";

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
            {...register("name", {
              required: "Nome e obrigatorio",
              validate: (value) =>
                value.trim().length >= 3 || "Nome deve ter ao menos 3 caracteres",
              maxLength: {
                value: 150,
                message: "Nome deve ter no maximo 150 caracteres",
              },
            })}
            placeholder="Nome do produto"
          />
          {errors.name && (
            <p className="text-xs text-destructive">{errors.name.message}</p>
          )}
        </div>

        <div className="space-y-1.5">
          <Label htmlFor="internalCode">Codigo Interno *</Label>
          <Input
            id="internalCode"
            {...register("internalCode", {
              required: "Codigo interno e obrigatorio",
              maxLength: {
                value: 50,
                message: "Codigo interno deve ter no maximo 50 caracteres",
              },
              validate: (value) =>
                /^[A-Za-z0-9._/-]+$/.test(value.trim()) ||
                "Use apenas letras, numeros, ponto, underscore, barra ou hifen",
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
          <Label htmlFor="barcode">Codigo de Barras</Label>
          <Input
            id="barcode"
            {...register("barcode", {
              maxLength: {
                value: 50,
                message: "Codigo de barras deve ter no maximo 50 caracteres",
              },
            })}
            placeholder="EAN-13 ou outro"
            className="font-mono"
          />
          {errors.barcode && (
            <p className="text-xs text-destructive">{errors.barcode.message}</p>
          )}
        </div>

        <div className="space-y-1.5">
          <Label htmlFor="price">Preco de Venda *</Label>
          <Input
            id="price"
            type="number"
            step="0.01"
            min="0.01"
            {...register("price", {
              required: "Preco e obrigatorio",
              min: { value: 0.01, message: "Preco deve ser maior que zero" },
              valueAsNumber: true,
            })}
            placeholder="0,00"
          />
          {errors.price && (
            <p className="text-xs text-destructive">{errors.price.message}</p>
          )}
        </div>

        <div className="space-y-1.5">
          <Label htmlFor="costPrice">Custo da Mercadoria *</Label>
          <Input
            id="costPrice"
            type="number"
            step="0.01"
            min="0.01"
            {...register("costPrice", {
              required: "Custo e obrigatorio",
              min: { value: 0.01, message: "Custo deve ser maior que zero" },
              valueAsNumber: true,
            })}
            placeholder="0,00"
          />
          {errors.costPrice && (
            <p className="text-xs text-destructive">
              {errors.costPrice.message}
            </p>
          )}
        </div>

        <div className="col-span-2 space-y-1.5">
          <Label htmlFor="description">Descricao</Label>
          <Textarea
            id="description"
            {...register("description", {
              maxLength: {
                value: 1000,
                message: "Descricao deve ter no maximo 1000 caracteres",
              },
            })}
            placeholder="Descricao detalhada do produto..."
            rows={3}
            className="h-24 min-h-24 max-h-24 resize-none overflow-y-auto [field-sizing:fixed]"
          />
          {errors.description && (
            <p className="text-xs text-destructive">
              {errors.description.message}
            </p>
          )}
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

export default function AdminProdutosPage() {
  const { activeCompany } = useCompany();
  const queryClient = useQueryClient();
  const [search, setSearch] = useState("");
  const [page, setPage] = useState(0);
  const [createOpen, setCreateOpen] = useState(false);
  const [editTarget, setEditTarget] = useState<ProductAdminResponse | null>(
    null,
  );
  const [deactivateTarget, setDeactivateTarget] =
    useState<ProductAdminResponse | null>(null);

  const {
    data: productPage,
    isLoading,
    error,
    refetch,
    isFetching,
  } = useQuery({
    queryKey: ["admin-produtos", page],
    queryFn: () => productAdminService.listPage(page, 25),
  });
  const products = productPage?.content ?? [];

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

  const { data: stockSummaryByProduct = {} } = useQuery({
    queryKey: [
      "admin-produtos-stock-summary",
      products.map((product) => product.id).join(","),
    ],
    enabled: products.length > 0,
    queryFn: async () => {
      const summaryEntries = await Promise.all(
        products.map(async (product) => {
          const stocks = await stockService
            .getByProduct(product.id)
            .catch(() => []);
          const totalQuantity = stocks.reduce(
            (accumulator, item) => accumulator + Number(item.quantity || 0),
            0,
          );
          const primaryLocation = [...stocks].sort(
            (a, b) => b.quantity - a.quantity,
          )[0];

          return [
            product.id,
            {
              stockQuantity: totalQuantity,
              warehouse: primaryLocation?.warehouseName ?? null,
              location: primaryLocation?.locationCode ?? null,
            },
          ] as const;
        }),
      );

      return Object.fromEntries(summaryEntries) as Record<
        string,
        {
          stockQuantity: number;
          warehouse: string | null;
          location: string | null;
        }
      >;
    },
    staleTime: 60_000,
  });

  return (
    <div
      className="flex h-full flex-col overflow-hidden"
      data-onboarding="admin-products-page"
    >
      <header
        className="flex items-center gap-3 border-b bg-card px-4 py-3 shadow-sm"
        data-onboarding="admin-products-header"
      >
        <Package className="h-5 w-5 text-primary" />
        <div>
            <h1 className="text-sm font-semibold leading-none">
              Cadastro de Produtos
            </h1>
            <p className="mt-0.5 text-xs text-muted-foreground">
              {isLoading
                ? "Carregando..."
                : `${productPage?.totalElements ?? 0} produto(s)`}
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

      <div className="border-b bg-primary/5 px-4 py-2 text-xs">
        <span className="font-semibold text-primary">Filial ativa:</span>{" "}
        <span className="text-foreground">
          {activeCompany?.name ?? "Nenhuma filial selecionada"}
        </span>
      </div>

      <div
        className="border-b bg-muted/30 px-4 py-3"
        data-onboarding="admin-products-filters"
      >
        <div className="relative max-w-xl">
          <Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
          <Input
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            placeholder="Buscar por nome, codigo interno ou codigo de barras..."
            className="bg-background pl-9"
          />
        </div>
      </div>

      <div className="flex-1 overflow-auto" data-onboarding="admin-products-content">
        {isLoading ? (
          <div className="flex h-40 items-center justify-center">
            <LoadingSpinner size="lg" label="Carregando produtos..." />
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
                  Cod. Interno
                </th>
                <th className="w-36 px-4 py-3 text-left text-xs font-semibold uppercase tracking-wide text-muted-foreground">
                  Cod. Barras
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
                  Venda
                </th>
                <th className="w-28 px-4 py-3 text-right text-xs font-semibold uppercase tracking-wide text-muted-foreground">
                  Custo
                </th>
                <th className="w-20 px-4 py-3 text-center text-xs font-semibold uppercase tracking-wide text-muted-foreground">
                  Status
                </th>
                <th className="w-20 px-4 py-3" />
              </tr>
            </thead>
            <tbody>
              {filtered.map((product, i) =>
                (() => {
                  const stockSummary = stockSummaryByProduct[product.id];
                  const quantity =
                    stockSummary?.stockQuantity ?? product.stockQuantity ?? 0;
                  const warehouse =
                    stockSummary?.warehouse ?? product.warehouse;
                  const location = stockSummary?.location ?? product.location;

                  return (
                    <tr
                      key={product.id}
                      className={`border-t transition-colors hover:bg-accent ${i % 2 !== 0 ? "bg-muted/20" : ""}`}
                    >
                      <td className="px-4 py-3 font-mono text-xs text-muted-foreground">
                        {product.internalCode}
                      </td>
                      <td className="px-4 py-3 font-mono text-xs text-muted-foreground">
                        {product.barcode ?? (
                          <span className="opacity-40">-</span>
                        )}
                      </td>
                      <td className="px-4 py-3 font-medium">{product.name}</td>
                      <td className="px-4 py-3 text-center font-medium">
                        {quantity}
                      </td>
                      <td className="px-4 py-3 text-xs text-muted-foreground">
                        {warehouse ? (
                          <span className="inline-flex flex-col">
                            <span className="font-semibold text-foreground">
                              {warehouse}
                            </span>
                            <span>{location}</span>
                          </span>
                        ) : (
                          <span className="opacity-40">-</span>
                        )}
                      </td>
                      <td className="px-4 py-3 text-right font-semibold tabular-nums">
                        {formatCurrency(product.price)}
                      </td>
                      <td className="px-4 py-3 text-right font-semibold tabular-nums text-amber-700">
                        {formatCurrency(product.costPrice)}
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
                  );
                })(),
              )}
            </tbody>
          </table>
        )}
      </div>

      <footer className="flex items-center justify-between border-t bg-card px-4 py-3 text-xs text-muted-foreground">
        <span>
          Pagina {productPage ? productPage.page + 1 : 1} de{" "}
          {productPage?.totalPages || 1}
        </span>
        <div className="flex items-center gap-2">
          <Button
            variant="outline"
            size="sm"
            onClick={() => setPage((current) => Math.max(current - 1, 0))}
            disabled={page === 0 || isFetching}
          >
            Anterior
          </Button>
          <Button
            variant="outline"
            size="sm"
            onClick={() =>
              setPage((current) =>
                productPage && current + 1 < productPage.totalPages
                  ? current + 1
                  : current,
              )
            }
            disabled={!productPage || page + 1 >= productPage.totalPages || isFetching}
          >
            Proxima
          </Button>
        </div>
      </footer>

      <Dialog open={createOpen} onOpenChange={setCreateOpen}>
        <DialogContent className="max-w-lg">
          <DialogHeader>
            <DialogTitle>Novo Produto</DialogTitle>
            <DialogDescription>
              Campos marcados com * sao obrigatorios.
            </DialogDescription>
          </DialogHeader>
          <ProductForm
            onSubmit={(data) =>
              createMutation.mutate(
                normalizeProductRequest({ ...data, active: true }),
              )
            }
            isPending={createMutation.isPending}
            onCancel={() => setCreateOpen(false)}
          />
        </DialogContent>
      </Dialog>

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
                  data: normalizeProductRequest({
                    ...data,
                    active: editTarget.active,
                  }),
                })
              }
              isPending={updateMutation.isPending}
              onCancel={() => setEditTarget(null)}
            />
          )}
        </DialogContent>
      </Dialog>

      <AlertDialog
        open={!!deactivateTarget}
        onOpenChange={(o) => !o && setDeactivateTarget(null)}
      >
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>Desativar produto?</AlertDialogTitle>
            <AlertDialogDescription>
              O produto <strong>{deactivateTarget?.name}</strong> sera marcado
              como inativo e nao aparecera mais nas buscas do PDV.
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
