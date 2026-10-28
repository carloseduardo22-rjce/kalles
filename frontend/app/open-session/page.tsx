"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import { useQuery, useQueryClient } from "@tanstack/react-query";
import { toast } from "sonner";
import { CompanySwitcher } from "@/components/company-switcher";
import { LogoutButton } from "@/components/logout-button";
import { OpenSessionForm } from "@/features/cash-register/components/open-session-form";
import { PairDeviceCard } from "@/features/cash-register/components/pair-device-card";
import { useSession } from "@/features/cash-register/hooks/use-session";
import { cashRegisterService } from "@/features/cash-register/services/cash-register.service";
import type { PairedDeviceResponse } from "@/features/cash-register/types";
import { ErrorAlert } from "@/shared/components/error-alert";
import { LoadingSpinner } from "@/shared/components/loading-spinner";
import { ApiError } from "@/shared/services/api";

const DEVICE_QUERY_KEY = ["pos-device"];

function isDeviceNotPaired(err: unknown): boolean {
  return (
    err instanceof ApiError &&
    (err.data as { code?: string } | undefined)?.code === "DEVICE_NOT_PAIRED"
  );
}

export default function OpenSessionPage() {
  const router = useRouter();
  const queryClient = useQueryClient();
  const { openSession, isLoading, error, clearError } = useSession();
  const [repairing, setRepairing] = useState(false);

  const device = useQuery({
    queryKey: DEVICE_QUERY_KEY,
    queryFn: cashRegisterService.getPairedDevice,
    retry: false,
  });
  const notPaired =
    device.error instanceof ApiError && device.error.status === 404;

  function handlePaired(paired: PairedDeviceResponse) {
    queryClient.setQueryData(DEVICE_QUERY_KEY, paired);
    queryClient.invalidateQueries({ queryKey: ["cash-registers"] });
    setRepairing(false);
    clearError();
    toast.success(`Navegador pareado com o caixa ${paired.cashRegisterCode}.`);
  }

  async function handleSuccess(
    cashRegisterCode: string,
    operatorCode: string,
    initialAmount: number,
    allowCashOnlyOperation = false,
  ) {
    try {
      await openSession(
        cashRegisterCode,
        operatorCode,
        initialAmount,
        allowCashOnlyOperation,
      );
      router.push("/pdv");
    } catch (err) {
      if (isDeviceNotPaired(err)) {
        queryClient.invalidateQueries({ queryKey: DEVICE_QUERY_KEY });
      }
    }
  }

  function renderBody() {
    if (device.isLoading) {
      return (
        <div className="flex min-h-[60vh] items-center justify-center">
          <LoadingSpinner size="lg" label="Verificando o pareamento…" />
        </div>
      );
    }
    if (notPaired || repairing) {
      return (
        <PairDeviceCard
          onPaired={handlePaired}
          onCancel={repairing ? () => setRepairing(false) : undefined}
        />
      );
    }
    if (device.isError || !device.data) {
      return (
        <div className="mx-auto max-w-md px-4 pt-8">
          <ErrorAlert
            error={device.error}
            title="Não foi possível verificar o pareamento"
          />
        </div>
      );
    }
    return (
      <OpenSessionForm
        onSuccess={handleSuccess}
        isLoading={isLoading}
        error={error}
        onClearError={clearError}
        pairedCashRegisterCode={device.data.cashRegisterCode}
        onRepair={() => setRepairing(true)}
      />
    );
  }

  return (
    <div className="min-h-screen bg-background">
      <div className="mx-auto flex w-full max-w-md items-center gap-2 px-4 pt-6">
        <div className="flex-1">
          <CompanySwitcher />
        </div>
        <LogoutButton />
      </div>
      {renderBody()}
    </div>
  );
}
