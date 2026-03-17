"use client";

import { useEffect, useState, useRef } from "react";
import { useTheme } from "next-themes";
import { Settings, ImageIcon, Trash2, Palette, Upload } from "lucide-react";
import { toast } from "sonner";

import { Button } from "@/components/ui/button";
import { Label } from "@/components/ui/label";
import { companySettingsService } from "@/shared/services/company-settings.service";

const themes = [
  { name: "Padrão (Azul/Branco)", value: "light", color: "bg-blue-600" },
  { name: "Esmeralda", value: "theme-emerald", color: "bg-emerald-600" },
  { name: "Âmbar", value: "theme-amber", color: "bg-amber-600" },
  { name: "Rosa", value: "theme-rose", color: "bg-rose-600" },
  { name: "Ardósia", value: "theme-slate", color: "bg-slate-800" },
  { name: "Escuro (Dark)", value: "dark", color: "bg-black" },
];

export default function ConfiguracoesPage() {
  const [logoUrl, setLogoUrl] = useState("");
  const [savedLogo, setSavedLogo] = useState<string | null>(null);
  const [previewError, setPreviewError] = useState(false);

  const { theme, setTheme } = useTheme();

  const fileInputRef = useRef<HTMLInputElement>(null);

  useEffect(() => {
    const stored = companySettingsService.getLogo();
    setSavedLogo(stored);
    if (stored) setLogoUrl(stored);
  }, []);

  function handleFileChange(e: React.ChangeEvent<HTMLInputElement>) {
    const file = e.target.files?.[0];
    if (!file) return;

    if (!file.type.startsWith("image/")) {
      toast.error("Por favor, selecione um arquivo de imagem válido.");
      return;
    }

    const reader = new FileReader();
    reader.onload = (event) => {
      const result = event.target?.result as string;
      setLogoUrl(result);
      setPreviewError(false);
    };
    reader.readAsDataURL(file);
  }

  function handleSaveLogo() {
    const trimmed = logoUrl.trim();
    if (!trimmed) {
      toast.error("Selecione uma imagem para salvar.");
      return;
    }
    companySettingsService.setLogo(trimmed);
    setSavedLogo(trimmed);
    setPreviewError(false);
    toast.success("Logo salva com sucesso.");
  }

  function handleRemoveLogo() {
    companySettingsService.removeLogo();
    setSavedLogo(null);
    setLogoUrl("");
    setPreviewError(false);
    if (fileInputRef.current) {
      fileInputRef.current.value = "";
    }
    toast.success("Logo removida.");
  }

  return (
    <div className="flex h-full flex-col overflow-hidden">
      {/* ─── Header ─── */}
      <header className="flex items-center gap-3 border-b bg-card px-4 py-3 shadow-sm">
        <Settings className="h-5 w-5 text-primary" />
        <div>
          <h1 className="text-sm font-semibold leading-none">Configurações</h1>
          <p className="mt-0.5 text-xs text-muted-foreground">
            Personalização da empresa
          </p>
        </div>
      </header>

      <div className="flex-1 overflow-auto p-4">
        <div className="mx-auto max-w-lg space-y-6">
          {/* ─── Themes section ─── */}
          <div className="rounded-lg border bg-card p-4 shadow-sm">
            <div className="mb-4 flex items-center gap-2">
              <Palette className="h-4 w-4 text-muted-foreground" />
              <h2 className="text-sm font-semibold">Tema e Cores</h2>
            </div>
            <p className="mb-4 text-xs text-muted-foreground">
              Selecione o template de cores padrão para os usuários do sistema.
            </p>

            <div className="grid grid-cols-2 gap-3 sm:grid-cols-3">
              {themes.map((t) => (
                <button
                  key={t.value}
                  onClick={() => setTheme(t.value)}
                  className={`flex flex-col items-center justify-center gap-2 rounded-md border p-3 transition-colors hover:bg-muted ${
                    theme === t.value
                      ? "border-primary bg-primary/5 ring-2 ring-primary/20"
                      : "border-border"
                  }`}
                >
                  <div
                    className={`h-6 w-6 rounded-full shadow-sm border border-white/10 ${t.color}`}
                  />
                  <span className="text-xs font-medium">{t.name}</span>
                </button>
              ))}
            </div>
          </div>

          {/* ─── Logo section ─── */}
          <div className="rounded-lg border bg-card p-4 shadow-sm">
            <div className="mb-4 flex items-center gap-2">
              <ImageIcon className="h-4 w-4 text-muted-foreground" />
              <h2 className="text-sm font-semibold">Logo da Empresa</h2>
            </div>
            <p className="mb-4 text-xs text-muted-foreground">
              A logo será exibida no canto superior esquerdo do Terminal PDV e
              nos recibos. Selecione uma imagem (PNG, JPG, SVG) do seu
              computador.
            </p>

            {/* Preview */}
            <div
              className="mb-4 flex items-center justify-center rounded-md border bg-muted/30 p-4 relative overflow-hidden group"
              style={{ minHeight: 96 }}
            >
              {logoUrl && !previewError ? (
                <img
                  src={logoUrl}
                  alt="Logo da empresa"
                  className="max-h-24 max-w-full object-contain"
                  onError={() => setPreviewError(true)}
                />
              ) : (
                <div className="flex flex-col items-center gap-1 text-muted-foreground/50">
                  <ImageIcon className="h-10 w-10" />
                  <p className="text-xs">
                    {previewError
                      ? "Não foi possível carregar a imagem"
                      : "Sem logo configurada"}
                  </p>
                </div>
              )}
            </div>

            <div className="space-y-3">
              <Label
                htmlFor="logoUpload"
                className="cursor-pointer flex w-full"
              >
                <div className="flex w-full items-center justify-center gap-2 rounded-md border border-dashed border-primary/50 bg-primary/5 p-4 text-sm font-medium text-primary hover:bg-primary/10 transition-colors">
                  <Upload className="h-4 w-4" />
                  Selecionar Imagem do Computador
                </div>
              </Label>
              <input
                ref={fileInputRef}
                id="logoUpload"
                type="file"
                accept="image/*"
                className="hidden"
                onChange={handleFileChange}
              />
            </div>

            <div className="mt-4 flex items-center gap-2">
              <Button onClick={handleSaveLogo} size="sm">
                Salvar Logo
              </Button>
              {savedLogo && (
                <Button
                  variant="ghost"
                  size="sm"
                  className="text-destructive hover:text-destructive"
                  onClick={handleRemoveLogo}
                >
                  <Trash2 className="mr-1.5 h-3.5 w-3.5" />
                  Remover
                </Button>
              )}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
