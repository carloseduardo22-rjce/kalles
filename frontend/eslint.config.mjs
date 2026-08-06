import next from "eslint-config-next";
import coreWebVitals from "eslint-config-next/core-web-vitals";
import typescript from "eslint-config-next/typescript";

const eslintConfig = [
  {
    ignores: [
      ".next/**",
      "node_modules/**",
      "cypress/screenshots/**",
      "cypress/videos/**",
      "cypress/downloads/**",
      "cypress/results/**",
      "next-env.d.ts",
    ],
  },
  ...[next, coreWebVitals, typescript].flatMap((c) =>
    Array.isArray(c) ? c : Array.isArray(c?.default) ? c.default : [c],
  ),
  {
    rules: {
      "@typescript-eslint/no-explicit-any": "warn",
      "@typescript-eslint/no-unused-vars": [
        "error",
        { argsIgnorePattern: "^_", varsIgnorePattern: "^_" },
      ],
      "react-hooks/set-state-in-effect": "warn",
      "react-hooks/purity": "warn",
    },
  },
  {
    files: ["cypress/**/*.ts"],
    rules: { "@typescript-eslint/no-unused-expressions": "off" },
  },
];

export default eslintConfig;
