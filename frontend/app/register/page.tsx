"use client";

import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { Eye, EyeOff } from "lucide-react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { useState } from "react";
import { toast } from "sonner";
import { api } from "@/shared/services/api";

export default function RegisterPage() {
  const [showPassword, setShowPassword] = useState(false);
  const [isLoading, setIsLoading] = useState(false);

  const [formData, setFormData] = useState({
    name: "",
    companyName: "",
    whatsapp: "",
    country: "br",
    email: "",
    password: "",
  });

  const router = useRouter();

  const handleRegister = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      setIsLoading(true);
      await api.post("/api/auth/register", {
        name: formData.name,
        companyName: formData.companyName,
        email: formData.email,
        password: formData.password,
      });
      toast.success("Conta criada! Verifique seu e-mail.");
      router.push(`/verify?email=${encodeURIComponent(formData.email)}`); // Redireciona para confirmação de email
    } catch (error: any) {
      toast.error(error.message || "Erro ao criar conta. Tente novamente.");
    } finally {
      setIsLoading(false);
    }
  };

  const handleChange =
    (field: string) => (e: React.ChangeEvent<HTMLInputElement>) => {
      setFormData((prev) => ({ ...prev, [field]: e.target.value }));
    };

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-50/50 p-4">
      <div className="w-full max-w-5xl bg-white rounded-3xl shadow-xl overflow-hidden flex flex-col md:flex-row min-h-150 p-2">
        {/* Cabeçalho / Lado Esquerdo */}
        <div className="w-full md:w-5/12 p-8 md:p-12 lg:p-16 flex flex-col justify-center border-b md:border-b-0 md:border-r border-gray-100">
          <div className="max-w-sm w-full mx-auto space-y-8">
            <div className="h-16 w-16 bg-red-600 rounded-2xl flex items-center justify-center text-white font-bold text-3xl shadow-sm">
              {/* Logo placeholder */}K
            </div>

            <div className="space-y-3">
              <h1 className="text-4xl sm:text-5xl font-semibold tracking-tight">
                Criar conta
              </h1>
              <Link
                href="/login"
                className="inline-block text-blue-600 hover:text-blue-700 font-medium text-lg"
              >
                Já tenho uma conta
              </Link>
            </div>

            <div className="pt-20 lg:pt-32">
              <p className="text-sm text-gray-500">
                Ao avançar você concorda com os{" "}
                <Link href="/terms" className="underline hover:text-gray-700">
                  Termos
                </Link>
                .
              </p>
            </div>
          </div>
        </div>

        {/* Formulário - Lado Direito */}
        <div className="w-full md:w-7/12 p-8 md:p-12 lg:p-16 flex flex-col justify-center">
          <div className="max-w-md w-full mx-auto">
            <form className="space-y-5" onSubmit={handleRegister}>
              <div className="flex flex-col sm:flex-row gap-4 items-start sm:items-center">
                <Label
                  htmlFor="name"
                  className="text-gray-700 sm:w-1/3 sm:text-right"
                >
                  Seu nome
                </Label>
                <div className="w-full sm:w-2/3">
                  <Input
                    id="name"
                    value={formData.name}
                    onChange={handleChange("name")}
                    className="h-11 rounded-lg w-full"
                    required
                  />
                </div>
              </div>

              <div className="flex flex-col sm:flex-row gap-4 items-start sm:items-center">
                <Label
                  htmlFor="companyName"
                  className="text-gray-700 sm:w-1/3 sm:text-right"
                >
                  Nome do Negócio
                </Label>
                <div className="w-full sm:w-2/3">
                  <Input
                    id="companyName"
                    value={formData.companyName}
                    onChange={handleChange("companyName")}
                    placeholder="Ex: Minha Loja"
                    className="h-11 rounded-lg w-full"
                    required
                  />
                </div>
              </div>

              <div className="flex flex-col sm:flex-row gap-4 items-start sm:items-center">
                <Label
                  htmlFor="whatsapp"
                  className="text-gray-700 sm:w-1/3 sm:text-right"
                >
                  WhatsApp
                </Label>
                <div className="w-full sm:w-2/3">
                  <Input
                    id="whatsapp"
                    type="tel"
                    value={formData.whatsapp}
                    onChange={handleChange("whatsapp")}
                    className="h-11 rounded-lg w-full"
                  />
                </div>
              </div>

              <div className="flex flex-col sm:flex-row gap-4 items-start sm:items-center">
                <Label
                  htmlFor="country"
                  className="text-gray-700 sm:w-1/3 sm:text-right"
                >
                  País
                </Label>
                <div className="w-full sm:w-2/3">
                  <Select
                    defaultValue={formData.country}
                    onValueChange={(val) =>
                      setFormData((prev) => ({ ...prev, country: val }))
                    }
                  >
                    <SelectTrigger className="h-11 rounded-lg w-full">
                      <SelectValue placeholder="Selecione o país" />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="br">🇧🇷 Brasil</SelectItem>
                      <SelectItem value="us">🇺🇸 Estados Unidos</SelectItem>
                      <SelectItem value="pt">🇵🇹 Portugal</SelectItem>
                    </SelectContent>
                  </Select>
                </div>
              </div>

              <div className="flex flex-col sm:flex-row gap-4 items-start sm:items-center">
                <Label
                  htmlFor="email"
                  className="text-gray-700 sm:w-1/3 sm:text-right"
                >
                  E-mail
                </Label>
                <div className="w-full sm:w-2/3">
                  <Input
                    id="email"
                    type="email"
                    value={formData.email}
                    onChange={handleChange("email")}
                    className="h-11 rounded-lg w-full"
                    required
                  />
                </div>
              </div>

              <div className="flex flex-col sm:flex-row gap-4 items-start sm:items-center">
                <Label
                  htmlFor="password"
                  className="text-gray-700 sm:w-1/3 sm:text-right"
                >
                  Senha
                </Label>
                <div className="w-full sm:w-2/3 relative">
                  <Input
                    id="password"
                    type={showPassword ? "text" : "password"}
                    value={formData.password}
                    onChange={handleChange("password")}
                    className="h-11 rounded-lg w-full pr-10"
                    required
                  />
                  <button
                    type="button"
                    onClick={() => setShowPassword(!showPassword)}
                    className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-500 hover:text-gray-700"
                  >
                    {showPassword ? (
                      <EyeOff className="h-4 w-4" />
                    ) : (
                      <Eye className="h-4 w-4" />
                    )}
                  </button>
                </div>
              </div>

              <div className="flex flex-col sm:flex-row gap-4 items-start sm:items-center pt-2">
                <div className="sm:w-1/3"></div>
                <div className="w-full sm:w-2/3">
                  <button
                    type="button"
                    className="text-sm text-gray-500 hover:text-gray-800 underline decoration-dotted underline-offset-4"
                  >
                    Possui um cupom? Clique aqui
                  </button>
                </div>
              </div>

              <div className="flex flex-col sm:flex-row gap-4 items-start sm:items-center mt-8 pt-4">
                <div className="sm:w-1/3"></div>
                <div className="w-full sm:w-2/3">
                  <Button
                    type="submit"
                    disabled={isLoading}
                    className="w-full bg-blue-500 hover:bg-blue-600 text-white rounded-lg h-12 text-base font-medium"
                  >
                    {isLoading ? "Criando..." : "Criar conta"}
                  </Button>
                </div>
              </div>
            </form>
          </div>
        </div>
      </div>
    </div>
  );
}
