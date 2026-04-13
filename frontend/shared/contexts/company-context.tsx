"use client";

import React, { createContext, useContext, useEffect, useState } from "react";
import { api } from "@/shared/services/api";
import {
  getSessionScopedItem,
  removeSessionScopedItem,
  setSessionScopedItem,
} from "@/shared/utils/session-storage";

export interface Company {
  id: string;
  name: string;
}

interface CompanyContextData {
  activeCompanyId: string | null;
  activeCompany: Company | null;
  companies: Company[];
  setActiveCompany: (companyId: string) => void;
  loadCompanies: (companies: Company[]) => void;
  loading: boolean;
}

const CompanyContext = createContext<CompanyContextData | undefined>(undefined);

const STORAGE_KEY = "@kalles:activeCompanyId";

export const CompanyProvider = ({
  children,
}: {
  children: React.ReactNode;
}) => {
  const [activeCompanyId, setActiveCompanyIdState] = useState<string | null>(
    null,
  );
  const [companies, setCompanies] = useState<Company[]>([]);
  const [loading, setLoading] = useState(true);

  const ensureValidSelection = (loadedCompanies: Company[]) => {
    if (!loadedCompanies.length) {
      setActiveCompanyIdState(null);
      removeSessionScopedItem(STORAGE_KEY);
      return;
    }

    const currentId = getSessionScopedItem(STORAGE_KEY);
    const currentStillExists = currentId
      ? loadedCompanies.some((company) => company.id === currentId)
      : false;

    if (currentStillExists && currentId) {
      setActiveCompanyIdState(currentId);
      return;
    }

    const fallbackId = loadedCompanies[0].id;
    setActiveCompanyIdState(fallbackId);
    setSessionScopedItem(STORAGE_KEY, fallbackId);
  };

  // Initialize from local storage on mount and fetch companies to populate the switcher
  useEffect(() => {
    const stored = getSessionScopedItem(STORAGE_KEY);
    if (stored) {
      setActiveCompanyIdState(stored);
    }

    // Fetch user's accessible companies based on their session/tenant
    api
      .get<Company[]>("/api/companies")
      .then((data) => {
        if (data && Array.isArray(data)) {
          setCompanies(data);
          ensureValidSelection(data);
        }
      })
      .catch((err) => {
        console.error(
          "Failed to load companies mapping for multi-tenant:",
          err,
        );
      })
      .finally(() => {
        setLoading(false);
      });
  }, []);

  const setActiveCompany = (id: string) => {
    setActiveCompanyIdState(id);
    setSessionScopedItem(STORAGE_KEY, id);
    // Reload page to reflect new company data
    window.location.reload();
  };

  const loadCompanies = (loaded: Company[]) => {
    setCompanies(loaded);
    ensureValidSelection(loaded);
  };

  const activeCompany = companies.find((c) => c.id === activeCompanyId) || null;

  return (
    <CompanyContext.Provider
      value={{
        activeCompanyId,
        activeCompany,
        companies,
        setActiveCompany,
        loadCompanies,
        loading,
      }}
    >
      {children}
    </CompanyContext.Provider>
  );
};

export const useCompany = (): CompanyContextData => {
  const context = useContext(CompanyContext);
  if (context === undefined) {
    throw new Error("useCompany must be used within a CompanyProvider");
  }
  return context;
};
