"use client";

import {
  createContext,
  useContext,
  useEffect,
  useMemo,
  useState,
  type ReactNode,
} from "react";

const PALETTE_STORAGE_KEY = "kalles:theme-palette";
const LEGACY_THEME_STORAGE_KEY = "theme";

export const paletteThemes = [
  "light",
  "theme-emerald",
  "theme-amber",
  "theme-rose",
  "theme-slate",
] as const;

export type PaletteTheme = (typeof paletteThemes)[number];

type AppearanceContextValue = {
  paletteTheme: PaletteTheme;
  setPaletteTheme: (theme: PaletteTheme) => void;
};

const AppearanceContext = createContext<AppearanceContextValue | undefined>(
  undefined,
);

function isPaletteTheme(value: string | null): value is PaletteTheme {
  return !!value && paletteThemes.includes(value as PaletteTheme);
}

function applyPaletteTheme(theme: PaletteTheme) {
  const root = document.documentElement;

  root.classList.remove(...paletteThemes.filter((item) => item !== "light"));

  if (theme !== "light") {
    root.classList.add(theme);
  }
}

export function AppearanceProvider({ children }: { children: ReactNode }) {
  const [paletteTheme, setPaletteThemeState] = useState<PaletteTheme>("light");

  useEffect(() => {
    const storedPaletteTheme = window.localStorage.getItem(PALETTE_STORAGE_KEY);
    const storedTheme =
      storedPaletteTheme ??
      window.localStorage.getItem(LEGACY_THEME_STORAGE_KEY);
    const initialTheme = isPaletteTheme(storedTheme) ? storedTheme : "light";

    setPaletteThemeState(initialTheme);
    applyPaletteTheme(initialTheme);

    if (storedPaletteTheme !== initialTheme) {
      window.localStorage.setItem(PALETTE_STORAGE_KEY, initialTheme);
    }
  }, []);

  const setPaletteTheme = (theme: PaletteTheme) => {
    setPaletteThemeState(theme);
    applyPaletteTheme(theme);
    window.localStorage.setItem(PALETTE_STORAGE_KEY, theme);
  };

  const value = useMemo(
    () => ({
      paletteTheme,
      setPaletteTheme,
    }),
    [paletteTheme],
  );

  return (
    <AppearanceContext.Provider value={value}>
      {children}
    </AppearanceContext.Provider>
  );
}

export function useAppearance() {
  const context = useContext(AppearanceContext);

  if (!context) {
    throw new Error("useAppearance must be used within an AppearanceProvider");
  }

  return context;
}
