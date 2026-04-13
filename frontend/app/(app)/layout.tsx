import { NavSidebar } from "@/components/nav-sidebar";

export default function AppLayout({ children }: { children: React.ReactNode }) {
  return (
    <div className="flex h-screen overflow-hidden bg-background">
      <NavSidebar />
      <main className="flex-1 overflow-auto" data-onboarding="page-shell">
        {children}
      </main>
    </div>
  );
}
