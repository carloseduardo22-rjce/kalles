"use client";

import { useEffect, useMemo, useState } from "react";
import { BookText, KeyRound, ShieldCheck } from "lucide-react";
import { toast } from "sonner";
import { TipTapEditor } from "@/components/ui/tip-tap-editor";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { api } from "@/shared/services/api";

type AuthMeResponse = {
  tenantId?: string;
};

type SensitiveEncryptResponse = {
  token: string;
};

type SensitiveDecryptResponse = {
  text: string;
};

type LocalSensitiveEntry = {
  secret: string;
  text: string;
};

const localSensitiveStore = new Map<string, LocalSensitiveEntry>();

export default function NotesPage() {
  const [title, setTitle] = useState("");
  const [content, setContent] = useState("");
  const [tenantId, setTenantId] = useState<string | null>(null);
  const [loadingTenant, setLoadingTenant] = useState(true);

  useEffect(() => {
    api
      .get<AuthMeResponse>("/api/auth/me")
      .then((data: AuthMeResponse | null) => {
        setTenantId(data?.tenantId ?? null);
      })
      .catch(() => {
        setTenantId(null);
      })
      .finally(() => {
        setLoadingTenant(false);
      });
  }, []);

  const noteStats = useMemo(() => {
    const plainText = content
      .replace(/<[^>]+>/g, " ")
      .replace(/\s+/g, " ")
      .trim();
    return {
      characters: plainText.length,
      sensitiveBlocks: (content.match(/data-sensitive=\"true\"/g) || []).length,
    };
  }, [content]);

  const handleEncryptSensitive = async (text: string, secret: string) => {
    const trimmedSecret = secret.trim();

    if (!trimmedSecret) {
      throw new Error("Informe um segredo valido.");
    }

    if (!tenantId) {
      const token = `local-${crypto.randomUUID()}`;
      localSensitiveStore.set(token, { text, secret: trimmedSecret });
      return token;
    }

    toast.info("Protegendo conteudo sensivel...");

    const data = await api.post<SensitiveEncryptResponse>(
      "/api/notes/sensitive/encrypt",
      {
        plainText: text,
        secret: trimmedSecret,
      },
    );

    return data.token;
  };

  const handleDecryptSensitive = async (token: string, secret: string) => {
    const trimmedSecret = secret.trim();

    if (!trimmedSecret) {
      throw new Error("Informe o segredo para continuar.");
    }

    if (token.startsWith("local-")) {
      const localEntry = localSensitiveStore.get(token);

      if (!localEntry || localEntry.secret !== trimmedSecret) {
        throw new Error("Segredo invalido para este conteudo.");
      }

      return localEntry.text;
    }

    if (!tenantId) {
      throw new Error(
        "Nao foi possivel validar este conteudo sem tenant ativo.",
      );
    }

    const data = await api.post<SensitiveDecryptResponse>(
      `/api/notes/sensitive/decrypt/${encodeURIComponent(token)}`,
      { secret: trimmedSecret },
    );

    return data.text;
  };

  return (
    <div className="min-h-screen bg-[radial-gradient(circle_at_top,_#fef3c7_0%,_#fff8eb_38%,_#f8fafc_100%)] px-4 py-8 md:px-8">
      <div className="mx-auto flex max-w-5xl flex-col gap-6">
        <section className="rounded-[28px] border border-amber-200/70 bg-white/85 p-6 shadow-[0_24px_80px_-40px_rgba(15,23,42,0.35)] backdrop-blur">
          <div className="flex flex-col gap-4 md:flex-row md:items-start md:justify-between">
            <div className="space-y-3">
              <div className="inline-flex items-center gap-2 rounded-full border border-amber-300 bg-amber-50 px-3 py-1 text-xs font-semibold uppercase tracking-[0.18em] text-amber-900">
                <ShieldCheck className="h-3.5 w-3.5" />
                Notas protegidas
              </div>
              <div>
                <h1 className="text-3xl font-semibold tracking-tight text-slate-900">
                  Bloco de notas
                </h1>
                <p className="mt-2 max-w-2xl text-sm leading-6 text-slate-600">
                  Escreva anotacoes normais e proteja apenas os trechos
                  sensiveis. Em vez de um blur confuso, a nota mostra um
                  marcador claro e, ao clicar nele, o sistema pede o mesmo
                  segredo usado na protecao para revelar o conteudo armazenado.
                </p>
              </div>
            </div>

            <div className="grid min-w-[220px] gap-3 rounded-2xl border border-slate-200 bg-slate-50/80 p-4 text-sm text-slate-600">
              <div className="flex items-center gap-2 font-medium text-slate-900">
                <BookText className="h-4 w-4 text-amber-700" />
                Resumo rapido
              </div>
              <div className="flex items-center justify-between">
                <span>Caracteres</span>
                <strong className="text-slate-900">
                  {noteStats.characters}
                </strong>
              </div>
              <div className="flex items-center justify-between">
                <span>Trechos sensiveis</span>
                <strong className="text-slate-900">
                  {noteStats.sensitiveBlocks}
                </strong>
              </div>
              <div className="flex items-start gap-2 rounded-xl bg-white px-3 py-2 text-xs text-slate-500">
                <KeyRound className="mt-0.5 h-3.5 w-3.5 shrink-0 text-amber-700" />
                <span>
                  {loadingTenant
                    ? "Preparando o tenant para criptografia no servidor..."
                    : tenantId
                      ? "Criptografia sensivel pronta para usar com o tenant atual."
                      : "Sem tenant carregado: a tela usa um fallback local temporario para nao travar a experiencia."}
                </span>
              </div>
            </div>
          </div>
        </section>

        <section className="rounded-[28px] border border-slate-200 bg-white p-6 shadow-[0_24px_80px_-48px_rgba(15,23,42,0.4)]">
          <div className="space-y-5">
            <Input
              type="text"
              value={title}
              onChange={(event) => setTitle(event.target.value)}
              placeholder="Titulo da nota"
              className="h-12 border-none px-0 text-2xl font-semibold tracking-tight text-slate-900 shadow-none focus-visible:ring-0"
            />

            <TipTapEditor
              content={content}
              onChange={setContent}
              onEncryptSensitive={handleEncryptSensitive}
              onDecryptSensitive={handleDecryptSensitive}
            />

            <div className="flex flex-col gap-3 border-t border-slate-200 pt-4 sm:flex-row sm:items-center sm:justify-between">
              <p className="text-sm text-slate-500">
                O botao de salvar ainda esta visual nesta tela, mas o fluxo de
                protecao sensivel ja foi melhorado e preparado para integracao.
              </p>
              <Button type="button" className="min-w-[140px]">
                Salvar nota
              </Button>
            </div>
          </div>
        </section>
      </div>
    </div>
  );
}
