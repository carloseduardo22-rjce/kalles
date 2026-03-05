"use client";

import { useEffect } from "react";
import { useRouter } from "next/navigation";
import { LoadingSpinner } from "@/shared/components/loading-spinner";

export default function RootPage() {
  const router = useRouter();

  useEffect(() => {
    router.replace("/relatorios");
  }, [router]);

  return (
    <div className="flex min-h-screen items-center justify-center bg-background">
      <LoadingSpinner size="lg" label="Carregando…" />
    </div>
  );
}
