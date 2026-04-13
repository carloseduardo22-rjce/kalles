import { Mark, mergeAttributes } from "@tiptap/core";

export const SensitiveInfo = Mark.create({
  name: "sensitiveInfo",

  addAttributes() {
    return {
      token: {
        default: null,
      },
      label: {
        default: "Informação sensível",
      },
    };
  },

  renderHTML({ HTMLAttributes }) {
    return [
      "span",
      mergeAttributes(HTMLAttributes, {
        class:
          "sensitive-info inline-flex cursor-pointer select-none items-center rounded-full border border-amber-300 bg-amber-50 px-3 py-1 text-xs font-semibold tracking-wide text-amber-950 shadow-sm transition-colors hover:bg-amber-100",
        title:
          "Informação sensível. Clique para informar o segredo e visualizar.",
        "data-sensitive": "true",
        "data-sensitive-token": HTMLAttributes.token,
        "data-sensitive-label": HTMLAttributes.label,
      }),
      0,
    ];
  },

  addCommands() {
    return {
      toggleSensitive:
        (options?: { token: string; label?: string }): any =>
        ({ commands }: { commands: any }) => {
          return commands.toggleMark(this.name, options);
        },
    } as any;
  },
});
