"use client";

import { useState, useEffect, useRef } from "react";
import { useQuery } from "@tanstack/react-query";
import { Search, Package, X } from "lucide-react";
import {
  Dialog,
  DialogContent,
  DialogClose,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { Input } from "@/components/ui/input";
import { Badge } from "@/components/ui/badge";
import { Spinner } from "@/components/ui/spinner";
import { productService } from "../services/product.service";
import { formatCurrency } from "@/shared/utils/formatters";
import type { ProductResponse } from "../types";

interface ProductLookupDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  /** Called with the internalCode of the selected product */
  onSelect: (internalCode: string) => void;
}

export function ProductLookupDialog({
  open,
  onOpenChange,
  onSelect,
}: ProductLookupDialogProps) {
  const [query, setQuery] = useState("");
  const [debouncedQuery, setDebouncedQuery] = useState("");
  const [highlightedIndex, setHighlightedIndex] = useState(0);
  const highlightedRowRef = useRef<HTMLTableRowElement | null>(null);
  const inputRef = useRef<HTMLInputElement | null>(null);

  // 250 ms debounce
  useEffect(() => {
    const id = setTimeout(() => setDebouncedQuery(query), 250);
    return () => clearTimeout(id);
  }, [query]);

  // Reset state when dialog opens
  useEffect(() => {
    if (open) {
      setQuery("");
      setDebouncedQuery("");
      setHighlightedIndex(0);
      setTimeout(() => inputRef.current?.focus(), 50);
    }
  }, [open]);

  // Reset highlight when results change
  useEffect(() => {
    setHighlightedIndex(0);
  }, [debouncedQuery]);

  // Scroll highlighted row into view
  useEffect(() => {
    highlightedRowRef.current?.scrollIntoView({ block: "nearest" });
  }, [highlightedIndex]);

  const isFiltering = debouncedQuery.length >= 2;

  const { data: products = [], isFetching } = useQuery({
    queryKey: ["products-lookup", debouncedQuery],
    queryFn: () =>
      isFiltering
        ? productService.search(debouncedQuery)
        : productService.getAll(),
    staleTime: 30_000,
    enabled: open,
  });

  function handleSelect(product: ProductResponse) {
    onSelect(product.internalCode);
    onOpenChange(false);
  }

  function handleClose(isOpen: boolean) {
    if (!isOpen) {
      setQuery("");
      setDebouncedQuery("");
      setHighlightedIndex(0);
    }
    onOpenChange(isOpen);
  }

  function handleKeyDown(e: React.KeyboardEvent<HTMLInputElement>) {
    if (e.key === "ArrowDown") {
      e.preventDefault();
      setHighlightedIndex((i) => Math.min(i + 1, products.length - 1));
    } else if (e.key === "ArrowUp") {
      e.preventDefault();
      setHighlightedIndex((i) => Math.max(i - 1, 0));
    } else if (e.key === "Enter") {
      e.preventDefault();
      if (products.length > 0) handleSelect(products[highlightedIndex]);
    }
  }

  return (
    <Dialog open={open} onOpenChange={handleClose}>
      <DialogContent
        showCloseButton={false}
        className="flex max-h-[90vh] max-w-5xl flex-col gap-0 overflow-hidden p-0"
      >
        {/* ─── Header ─── */}
        <DialogHeader className="flex-row items-center gap-3 border-b px-4 py-3">
          <Package className="h-4 w-4 shrink-0 text-primary" />
          <DialogTitle className="flex-1 text-sm">
            Consulta de Produtos
          </DialogTitle>
          <div className="flex items-center gap-3 text-xs text-muted-foreground">
            <span className="flex items-center gap-1">
              <kbd className="rounded border bg-muted px-1 py-0.5 font-mono text-[10px]">
                ↑↓
              </kbd>
              navegar
            </span>
            <span className="flex items-center gap-1">
              <kbd className="rounded border bg-muted px-1 py-0.5 font-mono text-[10px]">
                Enter
              </kbd>
              selecionar
            </span>
            <span className="flex items-center gap-1">
              <kbd className="rounded border bg-muted px-1 py-0.5 font-mono text-[10px]">
                Esc
              </kbd>
              fechar
            </span>
          </div>
          <DialogClose asChild>
            <button
              className="ml-2 flex h-7 w-7 shrink-0 items-center justify-center rounded-md text-muted-foreground transition-colors hover:bg-muted hover:text-foreground"
              aria-label="Fechar"
            >
              <X className="h-4 w-4" />
            </button>
          </DialogClose>
        </DialogHeader>

        {/* ─── Search bar ─── */}
        <div className="border-b bg-muted/30 px-4 py-3">
          <div className="relative">
            <Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
            {isFetching && (
              <Spinner className="absolute right-3 top-1/2 h-4 w-4 -translate-y-1/2" />
            )}
            <Input
              ref={inputRef}
              value={query}
              onChange={(e) => setQuery(e.target.value)}
              onKeyDown={handleKeyDown}
              placeholder="Pesquisar por nome, código interno ou código de barras…"
              className="border-0 bg-transparent pl-9 pr-9 text-sm shadow-none focus-visible:ring-0"
            />
          </div>
          <p className="mt-1 text-xs text-muted-foreground">
            {isFetching
              ? "Buscando…"
              : `${products.length} produto(s)${isFiltering ? ` para "${debouncedQuery}"` : " cadastrado(s)"}`}
          </p>
        </div>

        {/* ─── Results table ─── */}
        <div className="min-h-0 flex-1 overflow-x-hidden overflow-y-auto">
          {isFetching && products.length === 0 ? (
            <div className="flex items-center justify-center py-16">
              <Spinner className="h-6 w-6" />
            </div>
          ) : products.length === 0 ? (
            <div className="flex flex-col items-center justify-center py-16 text-muted-foreground">
              <Package className="mb-3 h-10 w-10 opacity-25" />
              <p className="text-sm font-medium">Nenhum produto encontrado</p>
              {isFiltering && (
                <p className="mt-1 text-xs">
                  Tente pesquisar por outros termos
                </p>
              )}
            </div>
          ) : (
            <table className="w-full table-fixed text-sm">
              <colgroup>
                <col className="w-[13%]" />
                <col className="w-[20%]" />
                <col className="w-[38%]" />
                <col className="w-[16%]" />
                <col className="w-[13%]" />
              </colgroup>
              <thead className="sticky top-0 border-b bg-muted">
                <tr>
                  <th className="px-3 py-2.5 text-left text-xs font-semibold uppercase tracking-wide text-muted-foreground">
                    Cód. Interno
                  </th>
                  <th className="px-3 py-2.5 text-left text-xs font-semibold uppercase tracking-wide text-muted-foreground">
                    Cód. Barras
                  </th>
                  <th className="px-3 py-2.5 text-left text-xs font-semibold uppercase tracking-wide text-muted-foreground">
                    Descrição
                  </th>
                  <th className="px-3 py-2.5 text-right text-xs font-semibold uppercase tracking-wide text-muted-foreground">
                    Preço Unit.
                  </th>
                  <th className="px-3 py-2.5 text-center text-xs font-semibold uppercase tracking-wide text-muted-foreground">
                    Estoque
                  </th>
                </tr>
              </thead>
              <tbody>
                {products.map((product, index) => {
                  const isHighlighted = index === highlightedIndex;
                  return (
                    <tr
                      key={product.id}
                      ref={isHighlighted ? highlightedRowRef : null}
                      className={`cursor-pointer border-t transition-colors ${
                        isHighlighted
                          ? "bg-primary text-primary-foreground"
                          : "hover:bg-accent"
                      }`}
                      onClick={() => handleSelect(product)}
                      onMouseEnter={() => setHighlightedIndex(index)}
                    >
                      <td
                        className={`px-3 py-3 font-mono text-xs ${isHighlighted ? "opacity-75" : "text-muted-foreground"}`}
                      >
                        {product.internalCode}
                      </td>
                      <td
                        className={`px-3 py-3 font-mono text-xs ${isHighlighted ? "opacity-75" : "text-muted-foreground"}`}
                      >
                        {product.barcode ?? "—"}
                      </td>
                      <td className="px-3 py-3 font-medium">{product.name}</td>
                      <td className="px-3 py-3 text-right font-semibold tabular-nums">
                        {formatCurrency(product.price)}
                      </td>
                      <td className="px-3 py-3 text-center">
                        <Badge
                          variant={
                            isHighlighted
                              ? "outline"
                              : product.stockQuantity <= 0
                                ? "destructive"
                                : product.stockQuantity <= 5
                                  ? "secondary"
                                  : "outline"
                          }
                          className={
                            isHighlighted
                              ? "border-primary-foreground/40 text-primary-foreground"
                              : ""
                          }
                        >
                          {product.stockQuantity}
                        </Badge>
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          )}
        </div>
      </DialogContent>
    </Dialog>
  );
}
