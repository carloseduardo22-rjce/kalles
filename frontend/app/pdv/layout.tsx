import { SignedInProviders } from "@/components/signed-in-providers";

export default function SignedInLayout({ children }: { children: React.ReactNode }) {
  return <SignedInProviders>{children}</SignedInProviders>;
}
