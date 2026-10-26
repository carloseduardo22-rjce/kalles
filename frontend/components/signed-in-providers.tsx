"use client";

import { PageOnboarding } from "@/components/onboarding/page-onboarding";
import { CompanyProvider } from "@/shared/contexts/company-context";

export function SignedInProviders({ children }: { children: React.ReactNode }) {
  return (
    <CompanyProvider>
      {children}
      <PageOnboarding />
    </CompanyProvider>
  );
}
