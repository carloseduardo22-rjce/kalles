"use client";

function canUseBrowserStorage(): boolean {
  return typeof window !== "undefined";
}

export function getSessionScopedItem(key: string): string | null {
  if (!canUseBrowserStorage()) {
    return null;
  }

  const currentValue = window.sessionStorage.getItem(key);
  if (currentValue !== null) {
    return currentValue;
  }

  const legacyValue = window.localStorage.getItem(key);
  if (legacyValue !== null) {
    window.sessionStorage.setItem(key, legacyValue);
    window.localStorage.removeItem(key);
  }

  return legacyValue;
}

export function setSessionScopedItem(key: string, value: string): void {
  if (!canUseBrowserStorage()) {
    return;
  }

  window.sessionStorage.setItem(key, value);
  window.localStorage.removeItem(key);
}

export function removeSessionScopedItem(key: string): void {
  if (!canUseBrowserStorage()) {
    return;
  }

  window.sessionStorage.removeItem(key);
  window.localStorage.removeItem(key);
}
