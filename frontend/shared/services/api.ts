const BASE_URL = process.env.NEXT_PUBLIC_API_URL ?? "http://localhost:8080";

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

async function handleResponse<T>(response: Response): Promise<T> {
  if (!response.ok) {
    let errorMessage = `Erro ${response.status}`;
    try {
      const errorData = await response.json();
      errorMessage =
        errorData.message ?? errorData.error ?? errorData.title ?? errorMessage;
    } catch {
      // ignore parse error
    }
    throw new ApiError(response.status, errorMessage);
  }

  if (response.status === 204) {
    return undefined as T;
  }

  return response.json() as Promise<T>;
}

type HttpHeaders = Record<string, string>;

export const api = {
  get: <T>(path: string, headers?: HttpHeaders): Promise<T> =>
    fetch(`${BASE_URL}${path}`, {
      method: "GET",
      headers: { "Content-Type": "application/json", ...headers },
    }).then(handleResponse<T>),

  post: <T>(path: string, body?: unknown, headers?: HttpHeaders): Promise<T> =>
    fetch(`${BASE_URL}${path}`, {
      method: "POST",
      headers: { "Content-Type": "application/json", ...headers },
      body: body !== undefined ? JSON.stringify(body) : undefined,
    }).then(handleResponse<T>),

  patch: <T>(path: string, body?: unknown, headers?: HttpHeaders): Promise<T> =>
    fetch(`${BASE_URL}${path}`, {
      method: "PATCH",
      headers: { "Content-Type": "application/json", ...headers },
      body: body !== undefined ? JSON.stringify(body) : undefined,
    }).then(handleResponse<T>),

  put: <T>(path: string, body?: unknown, headers?: HttpHeaders): Promise<T> =>
    fetch(`${BASE_URL}${path}`, {
      method: "PUT",
      headers: { "Content-Type": "application/json", ...headers },
      body: body !== undefined ? JSON.stringify(body) : undefined,
    }).then(handleResponse<T>),

  delete: <T>(
    path: string,
    params?: Record<string, string>,
    headers?: HttpHeaders,
  ): Promise<T> => {
    const url = new URL(`${BASE_URL}${path}`);
    if (params) {
      Object.entries(params).forEach(([k, v]) => url.searchParams.set(k, v));
    }
    return fetch(url.toString(), {
      method: "DELETE",
      headers: { "Content-Type": "application/json", ...headers },
    }).then(handleResponse<T>);
  },
};
