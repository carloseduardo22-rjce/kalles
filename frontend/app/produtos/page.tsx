"use client";

import { useState } from "react";
import { useQuery } from "@tanstack/react-query";
import { useRouter } from "next/navigation";
import {
  ArrowLeft,
  Search,
  Package,
  RefreshCw,
  ShoppingCart,
} from "lucide-react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Badge } from "@/components/ui/badge";
import { Separator } from "@/components/ui/separator";
import { LoadingSpinner } from "@/shared/components/loading-spinner";
import { productService } from "@/features/sales/services/product.service";
import { formatCurrency } from "@/shared/utils/formatters";
import type { ProductResponse } from "@/features/sales/types";
import { NavSidebar } from "@/components/nav-sidebar";

export default function ProdutosPage() {
  const router = useRouter();
  const [query, setQuery] = useState("");

  const {
    data: allProducts = [],
    isFetching,
    refetch,
  } = useQuery({
    queryKey: ["products-all"],
    queryFn: () => productService.getAll(),
    staleTime: 60_000,
  });

  // Client-side filter — fast, no extra round-trip for typing
  const filtered: ProductResponse[] =
    query.trim().length === 0
      ? allProducts
      : allProducts.filter((p) => {
          const q = query.toLowerCase();
          return (
            p.name.toLowerCase().includes(q) ||
            p.internalCode.toLowerCase().includes(q) ||
            (p.barcode ?? "").toLowerCase().includes(q)
          );
        });

  const inStockCount = allProducts.filter((p) => p.stockQuantity > 0).length;
  const lowStockCount = allProducts.filter(
    (p) => p.stockQuantity > 0 && p.stockQuantity <= 5,
  ).length;
  const outOfStockCount = allProducts.filter(
    (p) => p.stockQuantity <= 0,
  ).length;

  return (
    <div className="flex h-screen overflow-hidden bg-background">
      <NavSidebar />
      <div className="flex flex-1 flex-col overflow-hidden">
        {/* ─── Header ─── */}
        <header className="flex items-center gap-3 border-b bg-card px-4 py-3 shadow-sm">
          <Button
            variant="ghost"
            size="icon"
            className="shrink-0"
            onClick={() => router.back()}
          >
            <ArrowLeft className="h-4 w-4" />
          </Button>

          <div className="flex items-center gap-2">
            <Package className="h-5 w-5 text-primary" />
            <div>
              <h1 className="text-sm font-semibold leading-none">
                Consulta de Produtos
              </h1>
              <p className="mt-0.5 text-xs text-muted-foreground">
                {isFetching
                  ? "Carregando…"
                  : `${allProducts.length} produto(s) cadastrado(s)`}
              </p>
            </div>
          </div>

          {/* Summary badges */}
          {!isFetching && allProducts.length > 0 && (
            <>
              <Separator orientation="vertical" className="mx-1 h-8" />
              <div className="flex items-center gap-2 text-xs">
                <span className="flex items-center gap-1 text-muted-foreground">
                  <span className="h-2 w-2 rounded-full bg-green-500" />
                  {inStockCount} em estoque
                </span>
                {lowStockCount > 0 && (
                  <span className="flex items-center gap-1 text-amber-600">
                    <span className="h-2 w-2 rounded-full bg-amber-400" />
                    {lowStockCount} baixo estoque
                  </span>
                )}
                {outOfStockCount > 0 && (
                  <span className="flex items-center gap-1 text-destructive">
                    <span className="h-2 w-2 rounded-full bg-destructive" />
                    {outOfStockCount} sem estoque
                  </span>
                )}
              </div>
            </>
          )}

          <Button
            variant="ghost"
            size="icon"
            className="ml-auto shrink-0"
            onClick={() => refetch()}
            disabled={isFetching}
            title="Atualizar lista"
          >
            <RefreshCw
              className={`h-4 w-4 ${isFetching ? "animate-spin" : ""}`}
            />
          </Button>
        </header>
        {/* ─── Search bar ─── */}
        <div className="border-b bg-muted/30 px-4 py-3">
          <div className="relative max-w-xl">
            <Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
            <Input
              autoFocus
              value={query}
              onChange={(e) => setQuery(e.target.value)}
              placeholder="Pesquisar por nome, código interno ou código de barras…"
              className="border-primary/20 bg-background pl-9 focus-visible:border-primary"
            />
          </div>
          {query.trim().length > 0 && (
            <p className="mt-1.5 text-xs text-muted-foreground">
              {filtered.length} resultado(s) para &quot;{query}&quot;
              <button
                className="ml-2 text-primary underline-offset-2 hover:underline"
                onClick={() => setQuery("")}
              >
                limpar
              </button>
            </p>
          )}
        </div>
        {/* ─── Table ─── */}
        <div className="flex-1 overflow-auto">
          {isFetching && allProducts.length === 0 ? (
            <div className="flex h-full items-center justify-center">
              <LoadingSpinner size="lg" label="Carregando produtos…" />
            </div>
          ) : filtered.length === 0 ? (
            <div className="flex h-full flex-col items-center justify-center gap-3 text-muted-foreground">
              <Package className="h-14 w-14 opacity-20" />
              <p className="font-medium">Nenhum produto encontrado</p>
              {query && (
                <p className="text-sm">
                  Tente pesquisar por outros termos ou{" "}
                  <button
                    className="text-primary underline-offset-2 hover:underline"
                    onClick={() => setQuery("")}
                  >
                    limpe a busca
                  </button>
                </p>
              )}
            </div>
          ) : (
            <table className="w-full text-sm">
              <thead className="sticky top-0 z-10 border-b bg-muted shadow-sm">
                <tr>
                  <th className="w-8 px-4 py-3 text-center text-xs font-semibold uppercase tracking-wide text-muted-foreground">
                    #
                  </th>
                  <th className="w-28 px-4 py-3 text-left text-xs font-semibold uppercase tracking-wide text-muted-foreground">
                    Cód. Interno
                  </th>
                  <th className="w-36 px-4 py-3 text-left text-xs font-semibold uppercase tracking-wide text-muted-foreground">
                    Cód. Barras
                  </th>
                  <th className="px-4 py-3 text-left text-xs font-semibold uppercase tracking-wide text-muted-foreground">
                    Descrição
                  </th>
                  <th className="w-32 px-4 py-3 text-right text-xs font-semibold uppercase tracking-wide text-muted-foreground">
                    Preço Unit.
                  </th>
                  <th className="w-28 px-4 py-3 text-center text-xs font-semibold uppercase tracking-wide text-muted-foreground">
                    Estoque
                  </th>
                </tr>
              </thead>
              <tbody>
                {filtered.map((product, index) => (
                  <tr
                    key={product.id}
                    className={`border-t transition-colors hover:bg-accent ${
                      index % 2 !== 0 ? "bg-muted/20" : ""
                    }`}
                  >
                    <td className="px-4 py-3 text-center text-xs text-muted-foreground tabular-nums">
                      {index + 1}
                    </td>
                    <td className="px-4 py-3 font-mono text-xs text-muted-foreground">
                      {product.internalCode}
                    </td>
                    <td className="px-4 py-3 font-mono text-xs text-muted-foreground">
                      {product.barcode ?? <span className="opacity-40">—</span>}
                    </td>
                    <td className="px-4 py-3 font-medium">{product.name}</td>
                    <td className="px-4 py-3 text-right tabular-nums font-semibold">
                      {formatCurrency(product.price)}
                    </td>
                    <td className="px-4 py-3 text-center">
                      <Badge
                        variant={
                          product.stockQuantity <= 0
                            ? "destructive"
                            : product.stockQuantity <= 5
                              ? "secondary"
                              : "outline"
                        }
                        className={
                          product.stockQuantity > 5
                            ? "border-green-300 text-green-700 dark:border-green-800 dark:text-green-400"
                            : product.stockQuantity > 0
                              ? "border-amber-300 bg-amber-50 text-amber-700 dark:border-amber-800 dark:text-amber-400"
                              : ""
                        }
                      >
                        {product.stockQuantity > 0
                          ? product.stockQuantity
                          : "Esgotado"}
                      </Badge>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>
        {/* ─── Footer ─── */}
        {!isFetching && filtered.length > 0 && (
          <footer className="flex items-center justify-between border-t bg-card px-4 py-2 text-xs text-muted-foreground">
            <span>
              Exibindo {filtered.length} de {allProducts.length} produto(s)
            </span>
            <span>F2 no PDV para busca rápida</span>
          </footer>
        )}{" "}
      </div>{" "}
    </div>
  );
}
