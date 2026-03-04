"use client";

import { useState } from "react";
import { useForm } from "react-hook-form";
import { Unlock, Store, User, DollarSign } from "lucide-react";
import { Button } from "@/components/ui/button";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import {
  Form,
  FormControl,
  FormField,
  FormItem,
  FormLabel,
  FormMessage,
} from "@/components/ui/form";
import { Input } from "@/components/ui/input";
import { ErrorAlert } from "@/shared/components/error-alert";
import { LoadingSpinner } from "@/shared/components/loading-spinner";

interface FormValues {
  cashRegisterCode: string;
  operatorCode: string;
  initialAmount: string;
}

interface OpenSessionFormProps {
  onSuccess: (
    cashRegisterCode: string,
    operatorCode: string,
    initialAmount: number,
  ) => Promise<void>;
  isLoading: boolean;
  error: string | null;
  onClearError: () => void;
}

export function OpenSessionForm({
  onSuccess,
  isLoading,
  error,
  onClearError,
}: OpenSessionFormProps) {
  const form = useForm<FormValues>({
    defaultValues: {
      cashRegisterCode: "",
      operatorCode: "",
      initialAmount: "",
    },
  });

  async function onSubmit(values: FormValues) {
    const amount = parseFloat(values.initialAmount.replace(",", "."));
    if (isNaN(amount) || amount < 0) {
      form.setError("initialAmount", {
        message: "Informe um valor inicial válido (≥ 0)",
      });
      return;
    }
    await onSuccess(
      values.cashRegisterCode.trim(),
      values.operatorCode.trim(),
      amount,
    );
  }

  return (
    <div className="flex min-h-screen items-center justify-center bg-background p-4">
      <Card className="w-full max-w-md shadow-lg">
        <CardHeader className="space-y-1">
          <div className="flex items-center gap-2">
            <Store className="h-5 w-5 text-primary" />
            <CardTitle className="text-xl">Abertura de Caixa</CardTitle>
          </div>
          <CardDescription>
            Informe os dados do caixa e do operador para iniciar a sessão.
          </CardDescription>
        </CardHeader>

        <CardContent>
          {error && (
            <ErrorAlert
              error={error}
              title="Não foi possível abrir o caixa"
              className="mb-4"
            />
          )}

          <Form {...form}>
            <form
              onSubmit={form.handleSubmit(onSubmit)}
              onChange={onClearError}
              className="space-y-4"
            >
              <FormField
                control={form.control}
                name="cashRegisterCode"
                rules={{ required: "Código do caixa obrigatório" }}
                render={({ field }) => (
                  <FormItem>
                    <FormLabel className="flex items-center gap-1">
                      <Store className="h-3.5 w-3.5" />
                      Código do Caixa
                    </FormLabel>
                    <FormControl>
                      <Input
                        placeholder="ex: CAIXA-01"
                        autoComplete="off"
                        {...field}
                      />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />

              <FormField
                control={form.control}
                name="operatorCode"
                rules={{ required: "Código do operador obrigatório" }}
                render={({ field }) => (
                  <FormItem>
                    <FormLabel className="flex items-center gap-1">
                      <User className="h-3.5 w-3.5" />
                      Código do Operador
                    </FormLabel>
                    <FormControl>
                      <Input
                        placeholder="ex: OP-001"
                        autoComplete="off"
                        {...field}
                      />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />

              <FormField
                control={form.control}
                name="initialAmount"
                rules={{ required: "Valor inicial obrigatório" }}
                render={({ field }) => (
                  <FormItem>
                    <FormLabel className="flex items-center gap-1">
                      <DollarSign className="h-3.5 w-3.5" />
                      Valor Inicial em Espécie (R$)
                    </FormLabel>
                    <FormControl>
                      <Input
                        placeholder="0,00"
                        inputMode="decimal"
                        {...field}
                      />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />

              <Button type="submit" className="w-full" disabled={isLoading}>
                {isLoading ? (
                  <LoadingSpinner size="sm" label="" />
                ) : (
                  <>
                    <Unlock className="mr-2 h-4 w-4" />
                    Abrir Caixa
                  </>
                )}
              </Button>
            </form>
          </Form>
        </CardContent>
      </Card>
    </div>
  );
}
