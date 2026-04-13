"use client";

import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Eye, EyeOff } from "lucide-react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { useState } from "react";
import { toast } from "sonner";
import { api } from "@/shared/services/api";
import {
  getSessionScopedItem,
  setSessionScopedItem,
} from "@/shared/utils/session-storage";

export default function LoginPage() {
  const [showPassword, setShowPassword] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const router = useRouter();

  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      setIsLoading(true);
      const tenantId =
        typeof window !== "undefined"
          ? getSessionScopedItem(`@kalles:tenantId:${email}`)
          : null;
      await api.post("/api/auth/login", {
        email,
        password,
        tenantId: tenantId || undefined,
      });
      toast.success("Login realizado com sucesso!");

      // Consulta o perfil para decidir o redirecionamento por role
      try {
        const me = await api.get<{ role: string; tenantId?: string }>(
          "/api/auth/me",
        );
        if (me.tenantId) {
          setSessionScopedItem(`@kalles:tenantId:${email}`, me.tenantId);
        }
        if (me.role === "ADMIN") {
          router.push("/caixas");
        } else {
          router.push("/pdv");
        }
      } catch {
        // Fallback: se não conseguir consultar, vai para caixas
        router.push("/caixas");
      }
    } catch (error: any) {
      toast.error(error.message || "E-mail ou senha incorretos.");
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-50/50 p-4">
      <div className="w-full max-w-5xl bg-white rounded-3xl shadow-xl overflow-hidden flex min-h-150">
        {/* Formulário - Lado Esquerdo */}
        <div className="w-full md:w-1/2 p-8 md:p-12 lg:p-16 flex flex-col justify-center">
          <div className="max-w-md w-full mx-auto space-y-8">
            <div className="flex flex-col space-y-2">
              <div className="h-12 w-12 bg-red-600 rounded-xl flex items-center justify-center text-white font-bold text-xl mb-4">
                {/* Logo placeholder */}K
              </div>
              <div className="flex items-center justify-between">
                <h1 className="text-4xl font-semibold tracking-tight">Login</h1>
              </div>
              <p className="text-muted-foreground text-sm">
                Sem conta?{" "}
                <Link
                  href="/register"
                  className="text-blue-600 hover:text-blue-700 font-medium"
                >
                  Crie uma agora
                </Link>
              </p>
            </div>

            <form className="space-y-6" onSubmit={handleLogin}>
              <div className="space-y-2">
                <Label htmlFor="email" className="text-gray-700">
                  E-mail
                </Label>
                <Input
                  id="email"
                  type="email"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  placeholder="seu@email.com"
                  className="h-12 rounded-xl"
                  required
                />
              </div>

              <div className="space-y-2">
                <Label htmlFor="password" className="text-gray-700">
                  Senha
                </Label>
                <div className="relative">
                  <Input
                    id="password"
                    type={showPassword ? "text" : "password"}
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    placeholder="Sua senha secreta"
                    className="h-12 rounded-xl pr-10"
                    required
                  />
                  <button
                    type="button"
                    onClick={() => setShowPassword(!showPassword)}
                    className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-500 hover:text-gray-700"
                  >
                    {showPassword ? (
                      <EyeOff className="h-5 w-5" />
                    ) : (
                      <Eye className="h-5 w-5" />
                    )}
                  </button>
                </div>
              </div>

              <div className="flex items-center justify-between pt-2">
                <Link
                  href="/forgot-password"
                  className="text-sm font-medium text-blue-600 hover:text-blue-700"
                >
                  Esqueci a senha
                </Link>
                <Button
                  type="submit"
                  disabled={isLoading}
                  className="bg-blue-500 hover:bg-blue-600 text-white rounded-full px-8 h-12 text-base"
                >
                  {isLoading ? "Entrando..." : "Entrar"}
                </Button>
              </div>
            </form>

            <div className="pt-12 flex items-center justify-between text-sm text-gray-500">
              <select className="bg-transparent outline-none cursor-pointer">
                <option>Idioma: Português</option>
                <option>Idioma: English</option>
                <option>Idioma: Español</option>
              </select>
              <Link href="/terms" className="hover:underline">
                Termos de uso
              </Link>
            </div>
          </div>
        </div>

        {/* Imagem/Ilustração - Lado Direito */}
        <div className="hidden md:flex w-1/2 bg-blue-50 flex-col items-center justify-center p-12 relative overflow-hidden">
          {/* Elementos de background provisórios */}
          <div className="absolute bottom-0 w-full h-[30%] bg-blue-400 rounded-t-[100%] scale-150 transform translate-y-1/2"></div>

          <div className="relative z-10 text-center space-y-6 max-w-lg">
            <h2 className="text-5xl font-bold text-blue-500 tracking-tight">
              Oi, mundo!
            </h2>
            <p className="text-gray-700 text-lg">
              Use o catálogo online para expandir as fronteiras do seu comércio.
            </p>

            {/* Placeholder da imagem central */}
            <div className="mt-12 w-64 h-64 bg-gray-200/50 rounded-2xl mx-auto flex items-center justify-center border-2 border-dashed border-gray-300">
              <span className="text-sm text-gray-500">[Sua Imagem Aqui]</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
