"use client";

import { useState } from "react";
import { Barcode, Hash, ScanLine } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Tabs, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { LoadingSpinner } from "@/shared/components/loading-spinner";
import type { ProductCodeType } from "../types";

interface ProductSearchProps {
  isLoading: boolean;
  onAddItem: (type: ProductCodeType, code: string) => Promise<void>;
}

export function ProductSearch({ isLoading, onAddItem }: ProductSearchProps) {
  const [code, setCode] = useState("");
  const [type, setType] = useState<ProductCodeType>("INTERNAL_CODE");

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    const trimmed = code.trim();
    if (!trimmed) return;
    await onAddItem(type, trimmed);
    setCode("");
  }

  return (
    <form onSubmit={handleSubmit} className="space-y-2">
      <Tabs
        value={type}
        onValueChange={(v) => setType(v as ProductCodeType)}
        className="w-full"
      >
        <TabsList className="grid w-full grid-cols-2">
          <TabsTrigger value="INTERNAL_CODE" className="text-xs">
            <Hash className="mr-1 h-3 w-3" />
            Código Interno
          </TabsTrigger>
          <TabsTrigger value="BAR_CODE" className="text-xs">
            <Barcode className="mr-1 h-3 w-3" />
            Código de Barras
          </TabsTrigger>
        </TabsList>
      </Tabs>

      <div className="flex gap-2">
        <Input
          value={code}
          onChange={(e) => setCode(e.target.value)}
          placeholder={
            type === "INTERNAL_CODE" ? "ex: PRD-001" : "ex: 7891234567890"
          }
          autoComplete="off"
          autoFocus
          className="font-mono"
        />
        <Button type="submit" disabled={isLoading || !code.trim()}>
          {isLoading ? (
            <LoadingSpinner size="sm" label="" />
          ) : (
            <ScanLine className="h-4 w-4" />
          )}
        </Button>
      </div>
    </form>
  );
}
