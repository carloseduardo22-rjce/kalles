"use client";

import { useState } from "react";
import { useForm } from "react-hook-form";
import { Unlock, Store, DollarSign } from "lucide-react";
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
import { Label } from "@/components/ui/label";
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
import { ErrorAlert } from "@/shared/components/error-alert";
import { MoneyInput } from "@/shared/components/money-input";
import { LoadingSpinner } from "@/shared/components/loading-spinner";
import type { PairedDeviceResponse } from "@/features/cash-register/types";

interface FormValues {
  initialAmount: number;
}

interface OpenSessionFormProps {
  onSuccess: (
    cashRegisterCode: string,
    initialAmount: number,
    allowCashOnlyOperation?: boolean,
  ) => Promise<void>;
  isLoading: boolean;
  error: string | null;
  onClearError: () => void;
  device: PairedDeviceResponse;
  onRepair: () => void;
}

export function OpenSessionForm({
  onSuccess,
  isLoading,
  error,
  onClearError,
  device,
  onRepair,
}: OpenSessionFormProps) {
  const [cashOnlyWarningOpen, setCashOnlyWarningOpen] = useState(false);
  const [pendingSubmit, setPendingSubmit] = useState<FormValues | null>(null);

  const form = useForm<FormValues>({
    defaultValues: {
      initialAmount: 0,
    },
  });

  async function submitForm(
    values: FormValues,
    allowCashOnlyOperation = false,
  ) {
    await onSuccess(
      device.cashRegisterCode,
      values.initialAmount,
      allowCashOnlyOperation,
    );
  }

  async function onSubmit(values: FormValues) {
    if (!device.paymentIntegrationConfigured) {
      setPendingSubmit(values);
      setCashOnlyWarningOpen(true);
      return;
    }

    await submitForm(values, false);
  }

  async function confirmCashOnlyOperation() {
    if (!pendingSubmit) return;
    setCashOnlyWarningOpen(false);
    try {
      await submitForm(pendingSubmit, true);
    } finally {
      setPendingSubmit(null);
    }
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
            Informe o valor inicial para abrir a sessao em seu nome.
          </CardDescription>
        </CardHeader>

        <CardContent>
          {error && (
            <ErrorAlert
              error={error}
              title="Nao foi possivel abrir o caixa"
              className="mb-4"
            />
          )}

          <Form {...form}>
            <form
              onSubmit={form.handleSubmit(onSubmit)}
              onChange={onClearError}
              className="space-y-4"
            >
              <div className="space-y-2">
                <Label
                  htmlFor="paired-cash-register"
                  className="flex items-center gap-1"
                >
                  <Store className="h-3.5 w-3.5" />
                  Caixa
                </Label>
                <Input
                  id="paired-cash-register"
                  value={device.cashRegisterCode}
                  disabled
                />
              </div>

              <FormField
                control={form.control}
                name="initialAmount"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel className="flex items-center gap-1">
                      <DollarSign className="h-3.5 w-3.5" />
                      Valor Inicial em Especie
                    </FormLabel>
                    <FormControl>
                      <MoneyInput
                        name={field.name}
                        ref={field.ref}
                        onBlur={field.onBlur}
                        value={field.value}
                        onValueChange={field.onChange}
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
              <Button
                type="button"
                variant="link"
                className="w-full"
                onClick={onRepair}
              >
                Parear com outro caixa
              </Button>
            </form>
          </Form>
        </CardContent>
      </Card>

      <AlertDialog
        open={cashOnlyWarningOpen}
        onOpenChange={(open) => {
          setCashOnlyWarningOpen(open);
          if (!open) {
            setPendingSubmit(null);
          }
        }}
      >
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>Pagamento nao configurado</AlertDialogTitle>
            <AlertDialogDescription>
              Pagamento não configurado, neste caixa você apenas poderá operar
              com dinheiro mas não poderá receber pagamentos via pix, vouchers
              e cartões de crédito.
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel>Cancelar</AlertDialogCancel>
            <AlertDialogAction onClick={confirmCashOnlyOperation}>
              Continuar mesmo assim
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </div>
  );
}
