"use client";

import { useState } from "react";
import { useQuery } from "@tanstack/react-query";
import { Search, Package, RefreshCw } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { LoadingSpinner } from "@/shared/components/loading-spinner";
import { productService } from "@/features/sales/services/product.service";
import { formatCurrency } from "@/shared/utils/formatters";
import type { ProductResponse } from "@/features/sales/types";

export default function ProdutosPage() {
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

  return (
    <div className="flex h-full flex-col overflow-hidden">
      {/* ─── Header ─── */}
      <header className="flex items-center gap-3 border-b bg-card px-4 py-3 shadow-sm">
        <Package className="h-5 w-5 text-primary" />
        <div>
          <h1 className="text-sm font-semibold leading-none">
            Catálogo de Produtos
          </h1>
          <p className="mt-0.5 text-xs text-muted-foreground">
            {isFetching
              ? "Carregando…"
              : `${allProducts.length} produto(s) cadastrado(s)`}
          </p>
        </div>

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
      )}
    </div>
  );
}
