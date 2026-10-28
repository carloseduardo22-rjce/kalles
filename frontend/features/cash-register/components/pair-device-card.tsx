"use client";

import { useState } from "react";
import { Link2 } from "lucide-react";

import { Button } from "@/components/ui/button";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { ErrorAlert } from "@/shared/components/error-alert";
import { LoadingSpinner } from "@/shared/components/loading-spinner";
import { cashRegisterService } from "../services/cash-register.service";
import type { PairedDeviceResponse } from "../types";

interface PairDeviceCardProps {
  onPaired: (device: PairedDeviceResponse) => void;
  onCancel?: () => void;
}

export function PairDeviceCard({ onPaired, onCancel }: PairDeviceCardProps) {
  const [code, setCode] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    setSubmitting(true);
    setError(null);
    try {
      onPaired(
        await cashRegisterService.pairDevice(
          code.replace(/\s/g, "").toUpperCase(),
        ),
      );
    } catch (err) {
      setError(
        err instanceof Error ? err.message : "Falha ao parear o navegador.",
      );
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <div className="flex min-h-screen items-center justify-center bg-background p-4">
      <Card className="w-full max-w-md shadow-lg">
        <CardHeader className="space-y-1">
          <div className="flex items-center gap-2">
            <Link2 className="h-5 w-5 text-primary" />
            <CardTitle className="text-xl">Parear este navegador</CardTitle>
          </div>
          <CardDescription>
            Este navegador ainda não está pareado com um caixa. Peça ao
            administrador o código gerado na tela de Caixas.
          </CardDescription>
        </CardHeader>

        <CardContent>
          {error && (
            <ErrorAlert
              error={error}
              title="Não foi possível parear"
              className="mb-4"
            />
          )}

          <form onSubmit={handleSubmit} className="space-y-4">
            <div className="space-y-2">
              <Label htmlFor="pairing-code">Código de pareamento</Label>
              <Input
                id="pairing-code"
                data-testid="pairing-code-input"
                value={code}
                onChange={(e) => {
                  setCode(e.target.value);
                  setError(null);
                }}
                maxLength={9}
                autoComplete="off"
                autoFocus
                placeholder="Ex: K7P2MX9Q"
                className="font-mono uppercase tracking-widest"
              />
            </div>

            <Button
              type="submit"
              className="w-full"
              disabled={submitting || code.trim() === ""}
            >
              {submitting ? <LoadingSpinner size="sm" label="" /> : "Parear"}
            </Button>
            {onCancel && (
              <Button
                type="button"
                variant="ghost"
                className="w-full"
                onClick={onCancel}
                disabled={submitting}
              >
                Voltar
              </Button>
            )}
          </form>
        </CardContent>
      </Card>
    </div>
  );
}
