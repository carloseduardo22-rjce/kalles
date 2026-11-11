"use client";

import { useEffect } from "react";
import { useRouter } from "next/navigation";
import { useQuery } from "@tanstack/react-query";
import { Button } from "@/components/ui/button";
import { ErrorAlert } from "@/shared/components/error-alert";
import { LoadingSpinner } from "@/shared/components/loading-spinner";
import { authService } from "@/shared/services/auth.service";

export function AdminAreaGuard({ children }: { children: React.ReactNode }) {
  const router = useRouter();
  const me = useQuery({ queryKey: ["auth", "me"], queryFn: authService.me, retry: false });
  const isAdmin = me.data?.role === "ADMIN";

  useEffect(() => {
    if (me.data && !isAdmin) {
      router.replace("/pdv");
    }
  }, [me.data, isAdmin, router]);

  if (me.isError) {
    return (
      <div className="flex h-screen flex-col items-center justify-center gap-4 bg-background p-4">
        <ErrorAlert error={me.error} title="Não foi possível carregar sua conta" className="max-w-md" />
        <Button variant="outline" onClick={() => me.refetch()}>
          Tentar de novo
        </Button>
      </div>
    );
  }

  if (!isAdmin) {
    return (
      <div className="flex h-screen items-center justify-center bg-background">
        <LoadingSpinner size="lg" label="Carregando…" />
      </div>
    );
  }

  return <>{children}</>;
}
