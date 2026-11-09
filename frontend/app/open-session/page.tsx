"use client";

import { useEffect, useState } from "react";
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

  const current = useQuery({
    queryKey: ["current-session"],
    queryFn: cashRegisterService.getCurrentSession,
    retry: false,
    gcTime: 0,
  });
  const device = useQuery({
    queryKey: DEVICE_QUERY_KEY,
    queryFn: cashRegisterService.getPairedDevice,
    retry: false,
    gcTime: 0,
    enabled: !current.isPending,
  });
  const sessionOnThisDevice =
    !!current.data &&
    device.data?.cashRegisterId === current.data.cashRegisterId;

  useEffect(() => {
    if (sessionOnThisDevice) {
      router.replace("/pdv");
    }
  }, [sessionOnThisDevice, router]);
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
    initialAmount: number,
    allowCashOnlyOperation = false,
  ) {
    try {
      await openSession(
        cashRegisterCode,
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
    if (current.isPending || device.isPending || sessionOnThisDevice) {
      return (
        <div className="flex min-h-[60vh] items-center justify-center">
          <LoadingSpinner size="lg" label="Verificando o pareamento…" />
        </div>
      );
    }
    if (current.data) {
      return (
        <PairDeviceCard
          onPaired={handlePaired}
          notice={`Você tem uma sessão aberta no caixa ${current.data.cashRegisterCode}. Informe o código de pareamento desse caixa para continuar.`}
        />
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
        device={device.data}
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
