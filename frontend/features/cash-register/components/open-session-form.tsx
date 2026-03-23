"use client";

import { useEffect } from "react";
import { useForm } from "react-hook-form";
import {
  Unlock,
  Store,
  User,
  DollarSign,
  Loader2,
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
        (r) => r.paymentIntegrationConfigured && !r.hasActiveSession,
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

  async function onSubmit(values: FormValues) {
    const amountStr = values.initialAmount
      ? values.initialAmount.replace(",", ".")
      : "0";
    const amount = parseFloat(amountStr);
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
                    <Select
                      onValueChange={(val) => {
                        field.onChange(val);
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
                        {registers?.map((r) => (
                          <SelectItem
                            key={r.cashRegisterId}
                            value={r.code}
                            disabled={r.hasActiveSession}
                          >
                            <div className="flex items-center gap-2">
                              <span>
                                {r.code} - {r.description}
                              </span>
                              {r.paymentIntegrationConfigured && (
                                <span className="flex items-center text-[10px] uppercase font-bold text-emerald-600 bg-emerald-50 px-1.5 py-0.5 rounded-sm border border-emerald-200">
                                  <CreditCard className="size-3 mr-1" />
                                  MP Auth
                                </span>
                              )}
                              {r.hasActiveSession && (
                                <span className="text-xs text-muted-foreground mr-1.5">
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
                rules={{ required: "Código do operador obrigatório" }}
                render={({ field }) => (
                  <FormItem>
                    <FormLabel className="flex items-center gap-1">
                      <User className="h-3.5 w-3.5" />
                      Código do Operador
                    </FormLabel>
                    <Select
                      onValueChange={(val) => {
                        field.onChange(val);
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
                        {operators?.map((op) => (
                          <SelectItem key={op.id} value={op.code}>
                            <span className="font-mono text-muted-foreground mr-2">
                              {op.code}
                            </span>
                            {op.name}
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
