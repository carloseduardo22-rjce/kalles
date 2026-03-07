"use client";

import { useEffect, useState } from "react";
import { Settings, ImageIcon, Trash2 } from "lucide-react";
import { toast } from "sonner";

import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { companySettingsService } from "@/shared/services/company-settings.service";

export default function ConfiguracoesPage() {
  const [logoUrl, setLogoUrl] = useState("");
  const [savedLogo, setSavedLogo] = useState<string | null>(null);
  const [previewError, setPreviewError] = useState(false);

  useEffect(() => {
    const stored = companySettingsService.getLogo();
    setSavedLogo(stored);
    if (stored) setLogoUrl(stored);
  }, []);

  function handleSaveLogo() {
    const trimmed = logoUrl.trim();
    if (!trimmed) {
      toast.error("Informe uma URL de imagem válida.");
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
          {/* ─── Logo section ─── */}
          <div className="rounded-lg border bg-card p-4 shadow-sm">
            <div className="mb-4 flex items-center gap-2">
              <ImageIcon className="h-4 w-4 text-muted-foreground" />
              <h2 className="text-sm font-semibold">Logo da Empresa</h2>
            </div>
            <p className="mb-4 text-xs text-muted-foreground">
              A logo será exibida no canto superior esquerdo do Terminal PDV.
              Informe a URL de uma imagem (PNG, JPG, SVG).
            </p>

            {/* Preview */}
            <div
              className="mb-4 flex items-center justify-center rounded-md border bg-muted/30 p-4"
              style={{ minHeight: 96 }}
            >
              {savedLogo && !previewError ? (
                <img
                  src={savedLogo}
                  alt="Logo da empresa"
                  className="max-h-16 max-w-full object-contain"
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

            <div className="space-y-1.5">
              <Label htmlFor="logoUrl">URL da Logo</Label>
              <Input
                id="logoUrl"
                value={logoUrl}
                onChange={(e) => {
                  setLogoUrl(e.target.value);
                  setPreviewError(false);
                }}
                placeholder="https://exemplo.com/logo.png"
              />
            </div>

            <div className="mt-3 flex items-center gap-2">
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
