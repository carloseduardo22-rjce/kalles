import { Mark, mergeAttributes } from "@tiptap/core";

export const SensitiveInfo = Mark.create({
  name: "sensitiveInfo",

  addAttributes() {
    return {
      token: {
        default: null,
      },
    };
  },

  renderHTML({ HTMLAttributes }) {
    // Aplicamos classes do Tailwind para o desfoque
    return [
      "span",
      mergeAttributes(HTMLAttributes, {
        class:
          "sensitive-info bg-slate-800 text-transparent hover:text-slate-100 transition-all cursor-pointer rounded px-2 py-0.5 select-none blur-[4px] hover:blur-none",
        title: "Informação sensível (Clique e insira a senha para visualizar)",
        "data-sensitive": "true",
      }),
      0,
    ];
  },

  addCommands() {
    return {
      toggleSensitive:
        (options?: { token: string }) =>
        ({ commands }) => {
          return commands.toggleMark(this.name, options);
        },
    };
  },
});
