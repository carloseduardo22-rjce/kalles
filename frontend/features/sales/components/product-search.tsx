"use client";

import {
  forwardRef,
  useCallback,
  useEffect,
  useImperativeHandle,
  useRef,
  useState,
} from "react";
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

export interface ProductSearchHandle {
  focus: () => void;
}

const BARCODE_SETTLE_MS = 80;

function isValidBarcode(value: string) {
  return /^\d{8,14}$/.test(value);
}

function normalizeCode(value: string, type: ProductCodeType) {
  const trimmed = value.trim();
  if (type === "BAR_CODE") {
    return trimmed.replace(/\s+/g, "");
  }
  return trimmed.toUpperCase();
}

export const ProductSearch = forwardRef<ProductSearchHandle, ProductSearchProps>(
  function ProductSearch({ isLoading, onAddItem }, ref) {
  const [code, setCode] = useState("");
  const [type, setType] = useState<ProductCodeType>("INTERNAL_CODE");
  const inputRef = useRef<HTMLInputElement | null>(null);
  const autoSubmitTimeoutRef = useRef<number | null>(null);
  const isSubmittingRef = useRef(false);

  const focusInput = useCallback(() => {
    inputRef.current?.focus({ preventScroll: true });
  }, []);

  useImperativeHandle(
    ref,
    () => ({
      focus: focusInput,
    }),
    [focusInput],
  );

  useEffect(() => {
    return () => {
      if (autoSubmitTimeoutRef.current !== null) {
        window.clearTimeout(autoSubmitTimeoutRef.current);
      }
    };
  }, []);

  const submitCode = useCallback(
    async (rawCode: string, forcedType?: ProductCodeType) => {
      if (isSubmittingRef.current) return;

      const detectedType =
        forcedType ??
        (isValidBarcode(rawCode.trim().replace(/\s+/g, ""))
          ? "BAR_CODE"
          : type);
      const normalizedCode = normalizeCode(rawCode, detectedType);

      if (!normalizedCode) return;

      isSubmittingRef.current = true;
      try {
        await onAddItem(detectedType, normalizedCode);
        setCode("");
      } finally {
        isSubmittingRef.current = false;
        focusInput();
      }
    },
    [focusInput, onAddItem, type],
  );

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    await submitCode(code);
  }

  function handleChange(nextValue: string) {
    setCode(nextValue);

    const sanitized = nextValue.trim().replace(/\s+/g, "");
    const looksLikeBarcode = isValidBarcode(sanitized);

    if (looksLikeBarcode) {
      setType("BAR_CODE");
    }

    if (autoSubmitTimeoutRef.current !== null) {
      window.clearTimeout(autoSubmitTimeoutRef.current);
    }

    if (!looksLikeBarcode || isLoading) {
      return;
    }

    autoSubmitTimeoutRef.current = window.setTimeout(() => {
      void submitCode(sanitized, "BAR_CODE");
    }, BARCODE_SETTLE_MS);
  }

  return (
    <form onSubmit={handleSubmit} className="space-y-2">
      <Tabs
        value={type}
        onValueChange={(v) => {
          setType(v as ProductCodeType);
          setTimeout(focusInput, 0);
        }}
        className="w-full"
      >
        <TabsList className="grid w-full grid-cols-2">
          <TabsTrigger 
             value="INTERNAL_CODE" 
             className="text-xs"
             onPointerDown={(e) => e.preventDefault()}
          >
            <Hash className="mr-1 h-3 w-3" />
            Código Interno
          </TabsTrigger>
          <TabsTrigger 
             value="BAR_CODE" 
             className="text-xs"
             onPointerDown={(e) => e.preventDefault()}
          >
            <Barcode className="mr-1 h-3 w-3" />
            Código de Barras
          </TabsTrigger>
        </TabsList>
      </Tabs>

      <div className="flex gap-2">
        <Input
          ref={inputRef}
          value={code}
          onChange={(e) => handleChange(e.target.value)}
          onBlur={() => {
            if (autoSubmitTimeoutRef.current !== null) {
              window.clearTimeout(autoSubmitTimeoutRef.current);
            }
          }}
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
  },
);
