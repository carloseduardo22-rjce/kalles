"use client";

import { useEditor, EditorContent } from "@tiptap/react";
import StarterKit from "@tiptap/starter-kit";
import { Bold, Italic, List, ShieldAlert } from "lucide-react";
import { Button } from "@/components/ui/button";
import { SensitiveInfo } from "./sensitive-mark";

interface TipTapEditorProps {
  content: string;
  onChange: (html: string) => void;
  onEncryptSensitive: (text: string) => Promise<string>;
}

export function TipTapEditor({
  content,
  onChange,
  onEncryptSensitive,
}: TipTapEditorProps) {
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
          "prose prose-sm sm:prose lg:prose-lg xl:prose-2xl mx-auto focus:outline-none min-h-[200px] border p-4 rounded-md bg-white",
      },
    },
  });

  if (!editor) {
    return null;
  }

  const handleSensitiveProtect = async () => {
    const { from, to } = editor.state.selection;
    if (from === to) return; // No selection

    // Obter o texto selecionado
    const selectedText = editor.state.doc.textBetween(from, to, " ");

    // Ao invés de criptografar no front, enviamos pro back via a prop onEncryptSensitive.
    // O back retorna um token/id que referencia o conteúdo criptografado.
    const token = await onEncryptSensitive(selectedText);

    // Substituímos o texto real pelo token mascarado no editor
    editor
      .chain()
      .focus()
      .deleteSelection()
      .insertContent(token)
      .setTextSelection({ from, to: from + token.length }) // Selecionar o token recém inserido
      .toggleMark("sensitiveInfo", { token })
      .run();
  };

  return (
    <div className="flex flex-col gap-2">
      <div className="flex flex-wrap items-center gap-1 border p-2 rounded-md bg-muted/50">
        <Button
          variant="ghost"
          size="sm"
          onClick={() => editor.chain().focus().toggleBold().run()}
          className={editor.isActive("bold") ? "bg-muted" : ""}
        >
          <Bold className="w-4 h-4" />
        </Button>
        <Button
          variant="ghost"
          size="sm"
          onClick={() => editor.chain().focus().toggleItalic().run()}
          className={editor.isActive("italic") ? "bg-muted" : ""}
        >
          <Italic className="w-4 h-4" />
        </Button>
        <Button
          variant="ghost"
          size="sm"
          onClick={() => editor.chain().focus().toggleBulletList().run()}
          className={editor.isActive("bulletList") ? "bg-muted" : ""}
        >
          <List className="w-4 h-4" />
        </Button>

        <div className="h-6 w-px bg-border mx-1" />
        <Button
          variant="outline"
          size="sm"
          onClick={handleSensitiveProtect}
          className="text-red-600 hover:text-red-700 hover:bg-red-50 border-red-200"
          title="Proteger seleção (Criptografar no Servidor)"
        >
          <ShieldAlert className="w-4 h-4 mr-2" />
          Proteger Seleção
        </Button>
      </div>

      <EditorContent editor={editor} />

      <p className="text-xs text-muted-foreground mt-2">
        <ShieldAlert className="w-3 h-3 inline mr-1" />
        Dica de segurança: Selecione um texto crítico que você digitou e clique
        em "Proteger Seleção". O texto será enviado para o servidor,
        criptografado e um texto mascarado aparecerá no lugar.
      </p>
    </div>
  );
}
