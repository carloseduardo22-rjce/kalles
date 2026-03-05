"use client";

import { useState } from "react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { Layers, Search, Plus, RefreshCw, Package, MapPin } from "lucide-react";
import { useForm } from "react-hook-form";
import { toast } from "sonner";

import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogFooter,
  DialogDescription,
} from "@/components/ui/dialog";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { LoadingSpinner } from "@/shared/components/loading-spinner";
import { ErrorAlert } from "@/shared/components/error-alert";
import { stockService } from "@/features/admin/services/stock.service";
import { warehouseService } from "@/features/admin/services/warehouse.service";
import { productService } from "@/features/sales/services/product.service";
import type { StockRequest, StockResponse } from "@/features/admin/types";
import type { ProductResponse } from "@/features/sales/types";

/* ─── Stock Form ─────────────────────────────────────────────────────────── */
function StockForm({
  products,
  onSubmit,
  isPending,
  onCancel,
}: {
  products: ProductResponse[];
  onSubmit: (data: StockRequest) => void;
  isPending: boolean;
  onCancel: () => void;
}) {
  const [selectedWarehouseId, setSelectedWarehouseId] = useState<string>("");

  const { data: warehouses = [] } = useQuery({
    queryKey: ["admin-depositos"],
    queryFn: () => warehouseService.listAll(),
  });

  const { data: locations = [] } = useQuery({
    queryKey: ["admin-locations", selectedWarehouseId],
    queryFn: () => warehouseService.listLocations(selectedWarehouseId),
    enabled: !!selectedWarehouseId,
  });

  const {
    register,
    handleSubmit,
    setValue,
    watch,
    formState: { errors },
  } = useForm<StockRequest>();

  const locationId = watch("locationId");

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
      <div className="space-y-1.5">
        <Label>Produto *</Label>
        <Select onValueChange={(v) => setValue("productId", v)}>
          <SelectTrigger>
            <SelectValue placeholder="Selecione um produto" />
          </SelectTrigger>
          <SelectContent>
            {products.map((p) => (
              <SelectItem key={p.id} value={p.id}>
                <span className="font-mono text-xs text-muted-foreground">
                  {p.internalCode}
                </span>
                {" — "}
                {p.name}
              </SelectItem>
            ))}
          </SelectContent>
        </Select>
        {errors.productId && (
          <p className="text-xs text-destructive">{errors.productId.message}</p>
        )}
      </div>

      <div className="space-y-1.5">
        <Label>Depósito *</Label>
        <Select
          onValueChange={(v) => {
            setSelectedWarehouseId(v);
            setValue("locationId", "");
          }}
        >
          <SelectTrigger>
            <SelectValue placeholder="Selecione um depósito" />
          </SelectTrigger>
          <SelectContent>
            {warehouses.map((wh) => (
              <SelectItem key={wh.id} value={wh.id}>
                {wh.name}
              </SelectItem>
            ))}
          </SelectContent>
        </Select>
      </div>

      <div className="space-y-1.5">
        <Label>Localização *</Label>
        <Select
          disabled={!selectedWarehouseId || locations.length === 0}
          onValueChange={(v) => setValue("locationId", v)}
          value={locationId}
        >
          <SelectTrigger>
            <SelectValue
              placeholder={
                !selectedWarehouseId
                  ? "Selecione um depósito primeiro"
                  : locations.length === 0
                    ? "Sem localizações cadastradas"
                    : "Selecione uma localização"
              }
            />
          </SelectTrigger>
          <SelectContent>
            {locations.map((loc) => (
              <SelectItem key={loc.id} value={loc.id}>
                <span className="font-mono">{loc.code}</span>
                {loc.description && (
                  <span className="ml-1.5 text-muted-foreground">
                    — {loc.description}
                  </span>
                )}
              </SelectItem>
            ))}
          </SelectContent>
        </Select>
        {errors.locationId && (
          <p className="text-xs text-destructive">
            {errors.locationId.message}
          </p>
        )}
      </div>

      <div className="space-y-1.5">
        <Label htmlFor="quantity">Quantidade *</Label>
        <Input
          id="quantity"
          type="number"
          min="0"
          {...register("quantity", {
            required: "Quantidade é obrigatória",
            min: { value: 0, message: "Quantidade não pode ser negativa" },
            valueAsNumber: true,
          })}
          placeholder="0"
        />
        {errors.quantity && (
          <p className="text-xs text-destructive">{errors.quantity.message}</p>
        )}
      </div>

      <p className="text-xs text-muted-foreground">
        Se já existir um registro para este produto nesta localização, a
        quantidade será atualizada.
      </p>

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
type ViewMode = "by-product" | "by-location";

export default function EstoquePage() {
  const queryClient = useQueryClient();
  const [viewMode] = useState<ViewMode>("by-product");
  const [search, setSearch] = useState("");
  const [setStockOpen, setSetStockOpen] = useState(false);
  const [selectedProductId, setSelectedProductId] = useState<string | null>(
    null,
  );
  const [stockDetail, setStockDetail] = useState<StockResponse[] | null>(null);
  const [detailProductName, setDetailProductName] = useState<string>("");

  const { data: products = [], isLoading: loadingProducts } = useQuery({
    queryKey: ["products-all"],
    queryFn: () => productService.getAll(),
    staleTime: 60_000,
  });

  const setStockMutation = useMutation({
    mutationFn: (data: StockRequest) => stockService.setStock(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["stock-by-product"] });
      setSetStockOpen(false);
      toast.success("Estoque atualizado com sucesso.");
    },
    onError: (err: unknown) => {
      toast.error(
        err instanceof Error ? err.message : "Erro ao atualizar estoque.",
      );
    },
  });

  const { data: productStock = [], isFetching: fetchingStock } = useQuery({
    queryKey: ["stock-by-product", selectedProductId],
    queryFn: () => stockService.getByProduct(selectedProductId!),
    enabled: !!selectedProductId,
  });

  const filtered = search.trim()
    ? products.filter((p) => {
        const q = search.toLowerCase();
        return (
          p.name.toLowerCase().includes(q) ||
          p.internalCode.toLowerCase().includes(q)
        );
      })
    : products;

  const openDetail = async (productId: string, productName: string) => {
    setSelectedProductId(productId);
    setDetailProductName(productName);
  };

  // Update stockDetail whenever productStock changes
  const displayStock = selectedProductId ? productStock : stockDetail;

  return (
    <div className="flex h-full overflow-hidden">
      {/* ─── Left: Product list ─── */}
      <div className="flex w-72 shrink-0 flex-col border-r">
        <div className="flex items-center gap-2 border-b bg-card px-3 py-3">
          <Package className="h-4 w-4 text-primary" />
          <span className="text-sm font-semibold">Produtos</span>
          <Button
            variant="ghost"
            size="icon"
            className="ml-auto h-7 w-7"
            onClick={() => setSetStockOpen(true)}
            title="Definir estoque"
          >
            <Plus className="h-3.5 w-3.5" />
          </Button>
        </div>

        <div className="border-b px-2 py-2">
          <div className="relative">
            <Search className="absolute left-2.5 top-1/2 h-3.5 w-3.5 -translate-y-1/2 text-muted-foreground" />
            <Input
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              placeholder="Buscar produto…"
              className="h-8 bg-background pl-8 text-xs"
            />
          </div>
        </div>

        <div className="flex-1 overflow-auto">
          {loadingProducts ? (
            <div className="flex h-20 items-center justify-center">
              <LoadingSpinner size="sm" />
            </div>
          ) : (
            filtered.map((p) => (
              <button
                key={p.id}
                onClick={() => openDetail(p.id, p.name)}
                className={`w-full border-b px-3 py-2.5 text-left text-xs hover:bg-accent ${
                  selectedProductId === p.id ? "bg-primary/10 text-primary" : ""
                }`}
              >
                <p className="font-medium truncate">{p.name}</p>
                <p className="font-mono text-muted-foreground">
                  {p.internalCode}
                </p>
              </button>
            ))
          )}
        </div>
      </div>

      {/* ─── Right: Stock detail ─── */}
      <div className="flex flex-1 flex-col overflow-hidden">
        <header className="flex items-center gap-2 border-b bg-card px-4 py-3 shadow-sm">
          <Layers className="h-5 w-5 text-primary" />
          <div>
            <h1 className="text-sm font-semibold leading-none">
              Gestão de Estoque
            </h1>
            <p className="mt-0.5 text-xs text-muted-foreground">
              {selectedProductId
                ? detailProductName
                : "Selecione um produto para ver o estoque"}
            </p>
          </div>
          <Button
            size="sm"
            className="ml-auto"
            onClick={() => setSetStockOpen(true)}
          >
            <Plus className="mr-1.5 h-4 w-4" />
            Definir Estoque
          </Button>
        </header>

        <div className="flex-1 overflow-auto p-4">
          {!selectedProductId ? (
            <div className="flex h-full flex-col items-center justify-center gap-3 text-muted-foreground">
              <Layers className="h-12 w-12 opacity-20" />
              <p className="text-sm">
                Selecione um produto na lista para ver e gerenciar o estoque por
                localização.
              </p>
            </div>
          ) : fetchingStock ? (
            <div className="flex h-40 items-center justify-center">
              <LoadingSpinner size="lg" label="Carregando estoque…" />
            </div>
          ) : productStock.length === 0 ? (
            <div className="flex h-40 flex-col items-center justify-center gap-2 text-muted-foreground">
              <Layers className="h-10 w-10 opacity-20" />
              <p className="text-sm font-medium">
                Sem estoque registrado para este produto
              </p>
              <Button
                size="sm"
                variant="outline"
                onClick={() => setSetStockOpen(true)}
              >
                <Plus className="mr-1.5 h-4 w-4" />
                Definir Estoque
              </Button>
            </div>
          ) : (
            <div className="space-y-3">
              <div className="flex items-center gap-2 text-xs text-muted-foreground">
                <span>
                  Total disponível:{" "}
                  <strong className="text-foreground">
                    {productStock.reduce((s, r) => s + r.quantity, 0)} unidades
                  </strong>
                </span>
                <span>em {productStock.length} localização(ões)</span>
              </div>
              <div className="rounded-md border">
                <table className="w-full text-sm">
                  <thead className="border-b bg-muted/50">
                    <tr>
                      <th className="px-4 py-3 text-left text-xs font-semibold uppercase tracking-wide text-muted-foreground">
                        Depósito
                      </th>
                      <th className="w-36 px-4 py-3 text-left text-xs font-semibold uppercase tracking-wide text-muted-foreground">
                        Localização
                      </th>
                      <th className="w-28 px-4 py-3 text-right text-xs font-semibold uppercase tracking-wide text-muted-foreground">
                        Quantidade
                      </th>
                    </tr>
                  </thead>
                  <tbody>
                    {productStock.map((s, i) => (
                      <tr
                        key={s.id}
                        className={`border-t ${i % 2 !== 0 ? "bg-muted/20" : ""}`}
                      >
                        <td className="px-4 py-3 font-medium">
                          {s.warehouseName}
                        </td>
                        <td className="px-4 py-3">
                          <div className="flex items-center gap-1.5">
                            <MapPin className="h-3 w-3 text-muted-foreground" />
                            <span className="font-mono text-xs">
                              {s.locationCode}
                            </span>
                          </div>
                        </td>
                        <td className="px-4 py-3 text-right font-semibold tabular-nums">
                          {s.quantity}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>
          )}
        </div>
      </div>

      {/* ─── Set Stock Dialog ─── */}
      <Dialog open={setStockOpen} onOpenChange={setSetStockOpen}>
        <DialogContent className="max-w-md">
          <DialogHeader>
            <DialogTitle>Definir Estoque</DialogTitle>
            <DialogDescription>
              Informe o produto, a localização e a quantidade. Se já existir um
              registro para esse produto nessa localização, ele será atualizado.
            </DialogDescription>
          </DialogHeader>
          <StockForm
            products={products}
            onSubmit={(data) => setStockMutation.mutate(data)}
            isPending={setStockMutation.isPending}
            onCancel={() => setSetStockOpen(false)}
          />
        </DialogContent>
      </Dialog>
    </div>
  );
}
