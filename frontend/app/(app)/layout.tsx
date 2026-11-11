import { AdminAreaGuard } from "@/components/admin-area-guard";
import { NavSidebar } from "@/components/nav-sidebar";
import { SignedInProviders } from "@/components/signed-in-providers";

export default function AppLayout({ children }: { children: React.ReactNode }) {
  return (
    <AdminAreaGuard>
      <SignedInProviders>
        <div className="flex h-screen overflow-hidden bg-background">
          <NavSidebar />
          <main className="flex-1 overflow-auto" data-onboarding="page-shell">
            {children}
          </main>
        </div>
      </SignedInProviders>
    </AdminAreaGuard>
  );
}
