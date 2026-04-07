"use client";

import React, { createContext, useContext, useEffect, useState } from "react";
import { api } from "@/shared/services/api";

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

  // Initialize from local storage on mount and fetch companies to populate the switcher
  useEffect(() => {
    const stored = localStorage.getItem(STORAGE_KEY);
    if (stored) {
      setActiveCompanyIdState(stored);
    }

    // Fetch user's accessible companies based on their session/tenant
    api
      .get<Company[]>("/api/companies")
      .then((data) => {
        if (data && Array.isArray(data)) {
          setCompanies(data);
          // Auto-select first company if none is selected
          if (data.length > 0 && !stored) {
            const firstId = data[0].id;
            setActiveCompanyIdState(firstId);
            localStorage.setItem(STORAGE_KEY, firstId);
          }
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
    localStorage.setItem(STORAGE_KEY, id);
    // Reload page to reflect new company data
    window.location.reload();
  };

  const loadCompanies = (loaded: Company[]) => {
    setCompanies(loaded);
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
