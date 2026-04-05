"use client";

import { useState } from "react";
import { TipTapEditor } from "@/components/ui/tip-tap-editor";
import { toast } from "sonner";

export default function NotesPage() {
  const [content, setContent] = useState("");

  const handleEncryptSensitive = async (text: string) => {
    // Isso futuramente baterá no seu endpoint do backend /api/notes/sensitive/encrypt
    // Simulando o delay de request
    toast.info("Enviando para criptografia segura...");
    await new Promise((r) => setTimeout(r, 1000));
    toast.success("Protegido com sucesso!");
    return "token-seguro-" + Math.random().toString(36).substring(7);
  };

  return (
    <div className="p-8 max-w-5xl mx-auto">
      <div className="mb-6">
        <h1 className="text-2xl font-bold">Bloco de Notas</h1>
        <p className="text-gray-600">
          Anotações e informações úteis (suporta dados sensíveis
          criptografados).
        </p>
      </div>

      <div className="bg-white p-6 rounded-lg shadow-sm border">
        <input
          type="text"
          placeholder="Título da nota..."
          className="w-full text-xl font-semibold mb-4 focus:outline-none placeholder:text-gray-400"
        />

        <TipTapEditor
          content={content}
          onChange={setContent}
          onEncryptSensitive={handleEncryptSensitive}
        />

        <div className="flex justify-end mt-4">
          <button className="bg-blue-600 text-white px-6 py-2 rounded font-medium hover:bg-blue-700 transition">
            Salvar Nota
          </button>
        </div>
      </div>
    </div>
  );
}
