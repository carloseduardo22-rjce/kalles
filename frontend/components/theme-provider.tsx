'use client'

import * as React from 'react'
import {
  ThemeProvider as NextThemesProvider,
  type ThemeProviderProps,
} from 'next-themes'

const LEGACY_THEME_STORAGE_KEY = 'theme'
const COLOR_MODES = new Set(['light', 'dark'])

export function ThemeProvider({ children, ...props }: ThemeProviderProps) {
  React.useEffect(() => {
    const storageKey =
      typeof props.storageKey === 'string' ? props.storageKey : 'theme'

    if (storageKey === LEGACY_THEME_STORAGE_KEY) {
      return
    }

    const storedMode = window.localStorage.getItem(storageKey)
    if (storedMode) {
      return
    }

    const legacyTheme = window.localStorage.getItem(LEGACY_THEME_STORAGE_KEY)
    if (legacyTheme && COLOR_MODES.has(legacyTheme)) {
      window.localStorage.setItem(storageKey, legacyTheme)
    }
  }, [props.storageKey])

  return <NextThemesProvider {...props}>{children}</NextThemesProvider>
}
