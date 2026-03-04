"use client";

import { useEffect } from "react";
import { useRouter } from "next/navigation";
import { LoadingSpinner } from "@/shared/components/loading-spinner";

const STORAGE_KEY = "kalles:active-session";

export default function RootPage() {
  const router = useRouter();

  useEffect(() => {
    const raw = localStorage.getItem(STORAGE_KEY);
    if (raw) {
      router.replace("/pdv");
    } else {
      router.replace("/open-session");
    }
  }, [router]);

  return (
    <div className="flex min-h-screen items-center justify-center bg-background">
      <LoadingSpinner size="lg" label="Carregando…" />
    </div>
  );
}
