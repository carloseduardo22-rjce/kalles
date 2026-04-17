import {
  getSessionScopedItem,
  removeSessionScopedItem,
  setSessionScopedItem,
} from "@/shared/utils/session-storage";

const BASE_URL = process.env.NEXT_PUBLIC_API_URL ?? "";
const COMPANY_STORAGE_KEY = "@kalles:activeCompanyId";

export class ApiError extends Error {
  constructor(
    public readonly status: number,
    message: string,
    public readonly data?: unknown,
  ) {
    super(message);
    this.name = "ApiError";
  }
}

type HttpHeaders = Record<string, string>;

let refreshInFlight: Promise<boolean> | null = null;
let csrfInFlight: Promise<void> | null = null;
let csrfToken: string | null = null;
let companyContextInFlight: Promise<string | null> | null = null;

async function handleResponse<T>(response: Response): Promise<T> {
  if (!response.ok) {
    let errorMessage = `Erro ${response.status}`;
    let errorData: unknown;

    try {
      errorData = await response.json();

      if (errorData && typeof errorData === "object") {
        const parsedError = errorData as Record<string, unknown>;
        errorMessage =
          (typeof parsedError.detail === "string" && parsedError.detail) ||
          (typeof parsedError.message === "string" && parsedError.message) ||
          (typeof parsedError.error === "string" && parsedError.error) ||
          (typeof parsedError.title === "string" && parsedError.title) ||
          errorMessage;
      }
    } catch {
      // Ignore parse error and keep the default message.
    }

    throw new ApiError(response.status, errorMessage, errorData);
  }

  if (response.status === 204) {
    return undefined as T;
  }

  const text = await response.text();
  if (!text || text.trim() === "") {
    return undefined as T;
  }

  return JSON.parse(text) as T;
}

async function tryRefreshSession(): Promise<boolean> {
  if (!refreshInFlight) {
    refreshInFlight = fetch(buildUrl("/api/auth/refresh"), {
      method: "POST",
      credentials: "include",
    })
      .then((response) => response.ok)
      .catch(() => false)
      .finally(() => {
        refreshInFlight = null;
      });
  }

  return refreshInFlight;
}

const getActiveCompanyHeader = (input?: string): HttpHeaders => {
  if (typeof window === "undefined") {
    return {};
  }

  if (input && !isCompanyScopedApiRequest(input)) {
    return {};
  }

  const activeCompanyId = getSessionScopedItem(COMPANY_STORAGE_KEY);
  return activeCompanyId ? { "X-Company-ID": activeCompanyId } : {};
};

async function ensureCsrfToken(): Promise<void> {
  if (typeof window === "undefined") {
    return;
  }

  if (csrfToken) {
    return;
  }

  if (!csrfInFlight) {
    csrfInFlight = fetch(buildUrl("/api/auth/csrf"), {
      method: "GET",
      credentials: "include",
    })
      .then(async (response) => {
        if (!response.ok) {
          return;
        }

        const data = (await response.json()) as { token?: string };
        csrfToken = data.token ?? null;
      })
      .finally(() => {
        csrfInFlight = null;
      });
  }

  return csrfInFlight;
}

const buildHeaders = (headers?: HttpHeaders, input?: string): HttpHeaders => {
  return {
    "Content-Type": "application/json",
    ...getActiveCompanyHeader(input),
    ...(csrfToken ? { "X-XSRF-TOKEN": csrfToken } : {}),
    ...headers,
  };
};

const buildUrl = (
  path: string,
  params?: Record<string, string>,
): string => {
  const base = BASE_URL || window.location.origin;
  const url = new URL(path, base);

  if (params) {
    Object.entries(params).forEach(([key, value]) => {
      url.searchParams.set(key, value);
    });
  }

  return url.toString();
};

export const api = {
  get<T>(path: string, headers?: HttpHeaders): Promise<T> {
    const url = buildUrl(path);
    return requestWithRefresh<T>(url, {
      method: "GET",
      headers,
      credentials: "include",
    });
  },

  post<T>(path: string, body?: unknown, headers?: HttpHeaders): Promise<T> {
    const url = buildUrl(path);
    return requestWithRefresh<T>(url, {
      method: "POST",
      headers,
      body: body !== undefined ? JSON.stringify(body) : undefined,
      credentials: "include",
    });
  },

  patch<T>(path: string, body?: unknown, headers?: HttpHeaders): Promise<T> {
    const url = buildUrl(path);
    return requestWithRefresh<T>(url, {
      method: "PATCH",
      headers,
      body: body !== undefined ? JSON.stringify(body) : undefined,
      credentials: "include",
    });
  },

  put<T>(path: string, body?: unknown, headers?: HttpHeaders): Promise<T> {
    const url = buildUrl(path);
    return requestWithRefresh<T>(url, {
      method: "PUT",
      headers,
      body: body !== undefined ? JSON.stringify(body) : undefined,
      credentials: "include",
    });
  },

  delete<T>(
    path: string,
    params?: Record<string, string>,
    headers?: HttpHeaders,
  ): Promise<T> {
    const url = buildUrl(path, params);
    return requestWithRefresh<T>(url, {
      method: "DELETE",
      headers,
      credentials: "include",
    });
  },
};

function getApiPath(input: string): string {
  try {
    return new URL(input).pathname;
  } catch {
    return input;
  }
}

function isCompanyScopedApiRequest(input: string): boolean {
  const path = getApiPath(input);

  if (path.startsWith("/api/companies")) {
    return false;
  }

  return [
    "/api/products",
    "/api/warehouses",
    "/api/stocks",
    "/api/operators",
    "/api/cash-registers",
    "/api/cash-register-sessions",
    "/api/sales",
    "/api/clients",
    "/api/fidelity",
    "/api/fidelity-policies",
    "/api/goals",
    "/api/reports",
  ].some((prefix) => path.startsWith(prefix));
}

async function ensureCompanyContext(forceRefresh = false): Promise<string | null> {
  if (typeof window === "undefined") {
    return null;
  }

  const current = getSessionScopedItem(COMPANY_STORAGE_KEY);
  if (current && !forceRefresh) {
    return current;
  }

  if (forceRefresh) {
    removeSessionScopedItem(COMPANY_STORAGE_KEY);
  }

  if (!companyContextInFlight) {
    companyContextInFlight = resolveAuthenticatedCompanyId()
      .then((companyId) => companyId ?? resolveFirstAccessibleCompanyId())
      .then((companyId) => {
        if (companyId) {
          setSessionScopedItem(COMPANY_STORAGE_KEY, companyId);
        }
        return companyId;
      })
      .catch(() => null)
      .finally(() => {
        companyContextInFlight = null;
      });
  }

  return companyContextInFlight;
}

async function resolveAuthenticatedCompanyId(hasRetried = false): Promise<string | null> {
  const response = await fetch(buildUrl("/api/auth/me"), {
    method: "GET",
    credentials: "include",
    headers: { "Content-Type": "application/json" },
  });

  if (response.status === 401 && !hasRetried && (await tryRefreshSession())) {
    return resolveAuthenticatedCompanyId(true);
  }

  if (!response.ok) {
    return null;
  }

  const account = (await response.json()) as { companyId?: string | null };
  return account.companyId ?? null;
}

async function resolveFirstAccessibleCompanyId(hasRetried = false): Promise<string | null> {
  const response = await fetch(buildUrl("/api/companies"), {
    method: "GET",
    credentials: "include",
    headers: { "Content-Type": "application/json" },
  });

  if (response.status === 401 && !hasRetried && (await tryRefreshSession())) {
    return resolveFirstAccessibleCompanyId(true);
  }

  if (!response.ok) {
    return null;
  }

  const companies = (await response.json()) as Array<{ id?: string }>;
  return companies.find((company) => company.id)?.id ?? null;
}

async function requestWithRefresh<T>(
  input: string,
  init: RequestInit,
  hasRetried = false,
): Promise<T> {
  const method = (init.method ?? "GET").toUpperCase();
  const needsCompanyContext = isCompanyScopedApiRequest(input);
  const callerHeaders = stripManagedHeaders(init.headers as HttpHeaders | undefined);

  if (needsCompanyContext) {
    await ensureCompanyContext();
  }

  if (!["GET", "HEAD", "OPTIONS"].includes(method)) {
    await ensureCsrfToken();
  }

  init = {
    ...init,
    headers: buildHeaders(callerHeaders, input),
  };

  const response = await fetch(input, init);

  if (
    response.status === 403 &&
    needsCompanyContext &&
    !hasRetried &&
    (await isRecoverableCompanyContextForbidden(response))
  ) {
    await ensureCompanyContext(true);
    return requestWithRefresh<T>(input, init, true);
  }

  if (
    response.status === 403 &&
    !["GET", "HEAD", "OPTIONS"].includes(method) &&
    !hasRetried &&
    (await isRecoverableCsrfForbidden(response))
  ) {
    csrfToken = null;
    await ensureCsrfToken();
    return requestWithRefresh<T>(input, init, true);
  }

  if (
    response.status === 401 &&
    !hasRetried &&
    !input.endsWith("/api/auth/refresh") &&
    !input.endsWith("/api/auth/login")
  ) {
    const refreshed = await tryRefreshSession();

    if (refreshed) {
      return requestWithRefresh<T>(input, init, true);
    }
  }

  return handleResponse<T>(response);
}

async function isRecoverableCsrfForbidden(response: Response): Promise<boolean> {
  try {
    const data = (await response.clone().json()) as { code?: string };
    return data.code === "CSRF_INVALID" || data.code === "CSRF_MISSING";
  } catch {
    return false;
  }
}

async function isRecoverableCompanyContextForbidden(
  response: Response,
): Promise<boolean> {
  if (response.headers.get("X-Kalles-Company-Context-Error") === "true") {
    return true;
  }

  try {
    const text = await response.clone().text();
    return (
      text.includes("Company header conflicts with authenticated company") ||
      text.includes("Requested company is not accessible for authenticated tenant")
    );
  } catch {
    return false;
  }
}

function stripManagedHeaders(headers?: HttpHeaders): HttpHeaders {
  if (!headers) {
    return {};
  }

  return Object.fromEntries(
    Object.entries(headers).filter(([key]) => {
      const normalized = key.toLowerCase();
      return normalized !== "x-company-id" && normalized !== "x-xsrf-token";
    }),
  );
}
