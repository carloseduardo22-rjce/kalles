"use client";

import { useRouter } from "next/navigation";
import { CompanySwitcher } from "@/components/company-switcher";
import { OpenSessionForm } from "@/features/cash-register/components/open-session-form";
import { useSession } from "@/features/cash-register/hooks/use-session";

export default function OpenSessionPage() {
  const router = useRouter();
  const { openSession, isLoading, error, clearError } = useSession();

  async function handleSuccess(
    cashRegisterCode: string,
    operatorCode: string,
    initialAmount: number,
    allowCashOnlyOperation = false,
  ) {
    await openSession(
      cashRegisterCode,
      operatorCode,
      initialAmount,
      allowCashOnlyOperation,
    );
    router.push("/pdv");
  }

  return (
    <div className="min-h-screen bg-background">
      <div className="mx-auto w-full max-w-md px-4 pt-6">
        <CompanySwitcher />
      </div>
      <OpenSessionForm
        onSuccess={handleSuccess}
        isLoading={isLoading}
        error={error}
        onClearError={clearError}
      />
    </div>
  );
}
