"use client";

import { useEffect, useState } from "react";
import { useForm } from "react-hook-form";
import {
  Unlock,
  Store,
  User,
  DollarSign,
  CreditCard,
} from "lucide-react";
import { useQuery } from "@tanstack/react-query";
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
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { Input } from "@/components/ui/input";
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
import { LoadingSpinner } from "@/shared/components/loading-spinner";
import { cashRegisterService } from "@/features/cash-register/services/cash-register.service";

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
    allowCashOnlyOperation?: boolean,
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
  const [cashOnlyWarningOpen, setCashOnlyWarningOpen] = useState(false);
  const [pendingSubmit, setPendingSubmit] = useState<FormValues | null>(null);

  const form = useForm<FormValues>({
    defaultValues: {
      cashRegisterCode: "",
      operatorCode: "",
      initialAmount: "0,00",
    },
  });

  const { data: registers, isLoading: isLoadingRegisters } = useQuery({
    queryKey: ["cash-registers"],
    queryFn: cashRegisterService.listCashRegisters,
  });

  const { data: operators, isLoading: isLoadingOperators } = useQuery({
    queryKey: ["operators"],
    queryFn: cashRegisterService.listOperators,
  });

  useEffect(() => {
    if (
      registers &&
      registers.length > 0 &&
      !form.getValues().cashRegisterCode
    ) {
      const suggestedRegister = registers.find(
        (register) =>
          register.paymentIntegrationConfigured && !register.hasActiveSession,
      );
      if (suggestedRegister) {
        form.setValue("cashRegisterCode", suggestedRegister.code, {
          shouldValidate: true,
        });
      }
    }
  }, [registers, form]);

  useEffect(() => {
    if (operators && operators.length > 0 && !form.getValues().operatorCode) {
      form.setValue("operatorCode", operators[0].code, {
        shouldValidate: true,
      });
    }
  }, [operators, form]);

  async function submitForm(
    values: FormValues,
    allowCashOnlyOperation = false,
  ) {
    const amountStr = values.initialAmount
      ? values.initialAmount.replace(",", ".")
      : "0";
    const amount = parseFloat(amountStr);
    if (isNaN(amount) || amount < 0) {
      form.setError("initialAmount", {
        message: "Informe um valor inicial valido (>= 0)",
      });
      return;
    }

    await onSuccess(
      values.cashRegisterCode.trim(),
      values.operatorCode.trim(),
      amount,
      allowCashOnlyOperation,
    );
  }

  async function onSubmit(values: FormValues) {
    const selectedRegister = registers?.find(
      (register) => register.code === values.cashRegisterCode,
    );

    if (selectedRegister && !selectedRegister.paymentIntegrationConfigured) {
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
            Informe os dados do caixa e do operador para iniciar a sessao.
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
              <FormField
                control={form.control}
                name="cashRegisterCode"
                rules={{ required: "Codigo do caixa obrigatorio" }}
                render={({ field }) => (
                  <FormItem>
                    <FormLabel className="flex items-center gap-1">
                      <Store className="h-3.5 w-3.5" />
                      Codigo do Caixa
                    </FormLabel>
                    <Select
                      onValueChange={(value) => {
                        field.onChange(value);
                        onClearError();
                      }}
                      value={field.value}
                    >
                      <FormControl>
                        <SelectTrigger>
                          <SelectValue
                            placeholder={
                              isLoadingRegisters
                                ? "Carregando caixas..."
                                : "Selecione um caixa"
                            }
                          />
                        </SelectTrigger>
                      </FormControl>
                      <SelectContent>
                        {registers?.map((register) => (
                          <SelectItem
                            key={register.cashRegisterId}
                            value={register.code}
                            disabled={register.hasActiveSession}
                          >
                            <div className="flex items-center gap-2">
                              <span>
                                {register.code} - {register.description}
                              </span>
                              {register.paymentIntegrationConfigured && (
                                <span className="flex items-center rounded-sm border border-emerald-200 bg-emerald-50 px-1.5 py-0.5 text-[10px] font-bold uppercase text-emerald-600">
                                  <CreditCard className="mr-1 size-3" />
                                  MP Auth
                                </span>
                              )}
                              {register.hasActiveSession && (
                                <span className="mr-1.5 text-xs text-muted-foreground">
                                  (Em uso)
                                </span>
                              )}
                            </div>
                          </SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                    <FormMessage />
                  </FormItem>
                )}
              />

              <FormField
                control={form.control}
                name="operatorCode"
                rules={{ required: "Codigo do operador obrigatorio" }}
                render={({ field }) => (
                  <FormItem>
                    <FormLabel className="flex items-center gap-1">
                      <User className="h-3.5 w-3.5" />
                      Codigo do Operador
                    </FormLabel>
                    <Select
                      onValueChange={(value) => {
                        field.onChange(value);
                        onClearError();
                      }}
                      value={field.value}
                    >
                      <FormControl>
                        <SelectTrigger>
                          <SelectValue
                            placeholder={
                              isLoadingOperators
                                ? "Carregando operadores..."
                                : "Selecione um operador"
                            }
                          />
                        </SelectTrigger>
                      </FormControl>
                      <SelectContent>
                        {operators?.map((operator) => (
                          <SelectItem key={operator.id} value={operator.code}>
                            <span className="mr-2 font-mono text-muted-foreground">
                              {operator.code}
                            </span>
                            {operator.name}
                          </SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                    <FormMessage />
                  </FormItem>
                )}
              />

              <FormField
                control={form.control}
                name="initialAmount"
                rules={{ required: "Valor inicial obrigatorio" }}
                render={({ field }) => (
                  <FormItem>
                    <FormLabel className="flex items-center gap-1">
                      <DollarSign className="h-3.5 w-3.5" />
                      Valor Inicial em Especie (R$)
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
