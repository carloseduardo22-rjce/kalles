"use client";

import { useState } from "react";
import { EditorContent, useEditor } from "@tiptap/react";
import StarterKit from "@tiptap/starter-kit";
import { Bold, Eye, Italic, List, Loader2, ShieldAlert } from "lucide-react";
import { toast } from "sonner";
import { Button } from "@/components/ui/button";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { Input } from "@/components/ui/input";
import { SensitiveInfo } from "./sensitive-mark";

const SENSITIVE_PLACEHOLDER = "Informação sensível";

interface TipTapEditorProps {
  content: string;
  onChange: (html: string) => void;
  onEncryptSensitive: (text: string, secret: string) => Promise<string>;
  onDecryptSensitive: (token: string, secret: string) => Promise<string>;
}

export function TipTapEditor({
  content,
  onChange,
  onEncryptSensitive,
  onDecryptSensitive,
}: TipTapEditorProps) {
  const [protectOpen, setProtectOpen] = useState(false);
  const [revealOpen, setRevealOpen] = useState(false);
  const [selectedText, setSelectedText] = useState("");
  const [protectSecret, setProtectSecret] = useState("");
  const [protecting, setProtecting] = useState(false);
  const [revealSecret, setRevealSecret] = useState("");
  const [revealing, setRevealing] = useState(false);
  const [revealToken, setRevealToken] = useState("");
  const [revealedText, setRevealedText] = useState("");

  const editor = useEditor({
    immediatelyRender: false,
    extensions: [StarterKit, SensitiveInfo],
    content,
    onUpdate: ({ editor }) => {
      onChange(editor.getHTML());
    },
    editorProps: {
      attributes: {
        class:
          "prose prose-sm max-w-none min-h-[280px] rounded-2xl border border-slate-200 bg-white px-5 py-4 shadow-inner shadow-slate-100 focus:outline-none",
      },
      handleClick(_view, _pos, event) {
        const target = event.target as HTMLElement | null;
        const sensitiveElement = target?.closest(
          "[data-sensitive='true']",
        ) as HTMLElement | null;

        if (!sensitiveElement) {
          return false;
        }

        const token = sensitiveElement.dataset.sensitiveToken;
        if (!token) {
          return false;
        }

        setRevealToken(token);
        setRevealSecret("");
        setRevealedText("");
        setRevealOpen(true);
        return true;
      },
    },
  });

  if (!editor) {
    return null;
  }

  const handleSensitiveProtect = () => {
    const { from, to } = editor.state.selection;

    if (from === to) {
      toast.info("Selecione um trecho antes de proteger.");
      return;
    }

    const currentSelection = editor.state.doc.textBetween(from, to, " ").trim();
    if (!currentSelection) {
      toast.info("Selecione um texto válido para proteger.");
      return;
    }

    setSelectedText(currentSelection);
    setProtectSecret("");
    setProtectOpen(true);
  };

  const confirmSensitiveProtect = async () => {
    if (!protectSecret.trim()) {
      toast.error(
        "Informe o segredo que será exigido para revelar o conteúdo.",
      );
      return;
    }

    const { from, to } = editor.state.selection;
    if (from === to) {
      toast.error("A seleção foi perdida. Selecione o texto novamente.");
      setProtectOpen(false);
      return;
    }

    setProtecting(true);

    try {
      const token = await onEncryptSensitive(
        selectedText,
        protectSecret.trim(),
      );

      editor
        .chain()
        .focus()
        .deleteSelection()
        .insertContent(SENSITIVE_PLACEHOLDER)
        .setTextSelection({
          from,
          to: from + SENSITIVE_PLACEHOLDER.length,
        })
        .setMark("sensitiveInfo", {
          token,
          label: SENSITIVE_PLACEHOLDER,
        })
        .run();

      setProtectOpen(false);
      setSelectedText("");
      setProtectSecret("");
      toast.success("Trecho protegido com sucesso.");
    } catch (error) {
      toast.error(
        error instanceof Error
          ? error.message
          : "Não foi possível proteger esse conteúdo agora.",
      );
    } finally {
      setProtecting(false);
    }
  };

  const confirmRevealSensitive = async () => {
    if (!revealSecret.trim()) {
      toast.error("Informe o segredo para visualizar o conteúdo.");
      return;
    }

    setRevealing(true);

    try {
      const text = await onDecryptSensitive(revealToken, revealSecret.trim());
      setRevealedText(text);
    } catch (error) {
      toast.error(
        error instanceof Error
          ? error.message
          : "Não foi possível revelar esse conteúdo.",
      );
      setRevealedText("");
    } finally {
      setRevealing(false);
    }
  };

  return (
    <>
      <div className="flex flex-col gap-4">
        <div className="flex flex-wrap items-center gap-2 rounded-2xl border border-slate-200 bg-slate-50/80 p-2">
          <Button
            type="button"
            variant="ghost"
            size="sm"
            onClick={() => editor.chain().focus().toggleBold().run()}
            className={editor.isActive("bold") ? "bg-white shadow-sm" : ""}
          >
            <Bold className="h-4 w-4" />
          </Button>
          <Button
            type="button"
            variant="ghost"
            size="sm"
            onClick={() => editor.chain().focus().toggleItalic().run()}
            className={editor.isActive("italic") ? "bg-white shadow-sm" : ""}
          >
            <Italic className="h-4 w-4" />
          </Button>
          <Button
            type="button"
            variant="ghost"
            size="sm"
            onClick={() => editor.chain().focus().toggleBulletList().run()}
            className={
              editor.isActive("bulletList") ? "bg-white shadow-sm" : ""
            }
          >
            <List className="h-4 w-4" />
          </Button>

          <div className="mx-1 h-6 w-px bg-slate-200" />

          <Button
            type="button"
            variant="outline"
            size="sm"
            onClick={handleSensitiveProtect}
            className="border-amber-300 bg-white text-amber-900 hover:bg-amber-50"
            title="Proteger seleção"
          >
            <ShieldAlert className="mr-2 h-4 w-4" />
            Proteger seleção
          </Button>
        </div>

        <EditorContent editor={editor} />

        <div className="rounded-2xl border border-amber-200 bg-amber-50/70 px-4 py-3 text-sm text-amber-950">
          <div className="flex items-start gap-2">
            <ShieldAlert className="mt-0.5 h-4 w-4 shrink-0" />
            <p>
              Selecione um trecho e clique em <strong>Proteger seleção</strong>.
              O texto original sai da nota, fica armazenado com segurança e no
              lugar entra um marcador de informação sensível. Para ver de novo,
              basta clicar no marcador e informar o mesmo segredo usado na
              proteção.
            </p>
          </div>
        </div>
      </div>

      <Dialog open={protectOpen} onOpenChange={setProtectOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Proteger conteúdo sensível</DialogTitle>
            <DialogDescription>
              O trecho abaixo será substituído por um marcador na nota. Para
              revelar depois, será necessário informar este mesmo segredo.
            </DialogDescription>
          </DialogHeader>

          <div className="space-y-4">
            <div className="rounded-xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm text-slate-700">
              {selectedText}
            </div>

            <div className="space-y-2">
              <label className="text-sm font-medium text-slate-900">
                Segredo para revelar
              </label>
              <Input
                type="password"
                placeholder="Digite o segredo"
                value={protectSecret}
                onChange={(event) => setProtectSecret(event.target.value)}
                autoFocus
              />
            </div>
          </div>

          <DialogFooter>
            <Button
              type="button"
              variant="outline"
              onClick={() => setProtectOpen(false)}
              disabled={protecting}
            >
              Cancelar
            </Button>
            <Button
              type="button"
              onClick={confirmSensitiveProtect}
              disabled={protecting}
            >
              {protecting ? (
                <Loader2 className="mr-2 h-4 w-4 animate-spin" />
              ) : (
                <ShieldAlert className="mr-2 h-4 w-4" />
              )}
              Confirmar proteção
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      <Dialog open={revealOpen} onOpenChange={setRevealOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Revelar informação sensível</DialogTitle>
            <DialogDescription>
              Informe o segredo definido na proteção para ver o conteúdo
              armazenado.
            </DialogDescription>
          </DialogHeader>

          <div className="space-y-4">
            <div className="space-y-2">
              <label className="text-sm font-medium text-slate-900">
                Segredo
              </label>
              <Input
                type="password"
                placeholder="Digite o segredo"
                value={revealSecret}
                onChange={(event) => setRevealSecret(event.target.value)}
                autoFocus
              />
            </div>

            <div className="rounded-xl border border-slate-200 bg-slate-50 px-4 py-3">
              {revealedText ? (
                <div className="space-y-2">
                  <div className="flex items-center gap-2 text-sm font-medium text-emerald-700">
                    <Eye className="h-4 w-4" />
                    Conteúdo revelado
                  </div>
                  <p className="whitespace-pre-wrap text-sm text-slate-700">
                    {revealedText}
                  </p>
                </div>
              ) : (
                <p className="text-sm text-slate-500">
                  O conteúdo aparecerá aqui depois da validação do segredo.
                </p>
              )}
            </div>
          </div>

          <DialogFooter>
            <Button
              type="button"
              variant="outline"
              onClick={() => setRevealOpen(false)}
              disabled={revealing}
            >
              Fechar
            </Button>
            <Button
              type="button"
              onClick={confirmRevealSensitive}
              disabled={revealing}
            >
              {revealing ? (
                <Loader2 className="mr-2 h-4 w-4 animate-spin" />
              ) : (
                <Eye className="mr-2 h-4 w-4" />
              )}
              Revelar
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </>
  );
}
