import { getSessionScopedItem } from "@/shared/utils/session-storage";

const BASE_URL = process.env.NEXT_PUBLIC_API_URL ?? "";

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

const getActiveCompanyHeader = (): HttpHeaders => {
  if (typeof window === "undefined") {
    return {};
  }

  const activeCompanyId = getSessionScopedItem("@kalles:activeCompanyId");
  return activeCompanyId ? { "X-Company-ID": activeCompanyId } : {};
};

const getCookieValue = (name: string): string | null => {
  if (typeof document === "undefined") {
    return null;
  }

  const match = document.cookie.match(
    new RegExp(`(?:^|; )${name.replace(/[-[\]/{}()*+?.\\^$|]/g, "\\$&")}=([^;]*)`),
  );
  return match ? decodeURIComponent(match[1]) : null;
};

async function ensureCsrfToken(): Promise<void> {
  if (typeof window === "undefined") {
    return;
  }

  if (getCookieValue("XSRF-TOKEN")) {
    return;
  }

  if (!csrfInFlight) {
    csrfInFlight = fetch(buildUrl("/api/auth/csrf"), {
      method: "GET",
      credentials: "include",
    })
      .then(() => undefined)
      .finally(() => {
        csrfInFlight = null;
      });
  }

  return csrfInFlight;
}

const buildHeaders = (headers?: HttpHeaders): HttpHeaders => {
  const csrfToken = getCookieValue("XSRF-TOKEN");

  return {
    "Content-Type": "application/json",
    ...getActiveCompanyHeader(),
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
    return requestWithRefresh<T>(buildUrl(path), {
      method: "GET",
      headers: buildHeaders(headers),
      credentials: "include",
    });
  },

  post<T>(path: string, body?: unknown, headers?: HttpHeaders): Promise<T> {
    return requestWithRefresh<T>(buildUrl(path), {
      method: "POST",
      headers: buildHeaders(headers),
      body: body !== undefined ? JSON.stringify(body) : undefined,
      credentials: "include",
    });
  },

  patch<T>(path: string, body?: unknown, headers?: HttpHeaders): Promise<T> {
    return requestWithRefresh<T>(buildUrl(path), {
      method: "PATCH",
      headers: buildHeaders(headers),
      body: body !== undefined ? JSON.stringify(body) : undefined,
      credentials: "include",
    });
  },

  put<T>(path: string, body?: unknown, headers?: HttpHeaders): Promise<T> {
    return requestWithRefresh<T>(buildUrl(path), {
      method: "PUT",
      headers: buildHeaders(headers),
      body: body !== undefined ? JSON.stringify(body) : undefined,
      credentials: "include",
    });
  },

  delete<T>(
    path: string,
    params?: Record<string, string>,
    headers?: HttpHeaders,
  ): Promise<T> {
    return requestWithRefresh<T>(buildUrl(path, params), {
      method: "DELETE",
      headers: buildHeaders(headers),
      credentials: "include",
    });
  },
};

async function requestWithRefresh<T>(
  input: string,
  init: RequestInit,
  hasRetried = false,
): Promise<T> {
  const method = (init.method ?? "GET").toUpperCase();
  if (!["GET", "HEAD", "OPTIONS"].includes(method)) {
    await ensureCsrfToken();
    init = {
      ...init,
      headers: buildHeaders(init.headers as HttpHeaders | undefined),
    };
  }

  const response = await fetch(input, init);

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
