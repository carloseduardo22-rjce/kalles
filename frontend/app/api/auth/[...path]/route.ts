import { NextRequest, NextResponse } from "next/server";

const BACKEND_URL =
  process.env.BACKEND_URL ?? "http://localhost:8080";

const AUTH_COOKIE_NAME = "kalles_auth_token";
const REFRESH_COOKIE_NAME = "kalles_refresh_token";

type CookieAttributes = {
  maxAge: number;
};

function splitSetCookieHeader(value: string | null): string[] {
  if (!value) {
    return [];
  }

  return value.split(/,(?=\s*[^;,\s]+=)/g).map((item) => item.trim());
}

function extractSetCookieHeaders(response: Response): string[] {
  const candidate = response.headers as Headers & {
    getSetCookie?: () => string[];
  };

  if (typeof candidate.getSetCookie === "function") {
    const values = candidate.getSetCookie();
    if (values.length > 0) {
      return values;
    }
  }

  return splitSetCookieHeader(response.headers.get("set-cookie"));
}

function parseCookieHeader(cookieHeader: string): {
  name: string;
  value: string;
  attributes: CookieAttributes;
} | null {
  const parts = cookieHeader.split(";").map((part) => part.trim());
  const [nameValue, ...attributes] = parts;
  const separatorIndex = nameValue.indexOf("=");

  if (separatorIndex === -1) {
    return null;
  }

  const name = nameValue.slice(0, separatorIndex).trim();
  const value = nameValue.slice(separatorIndex + 1).trim();
  const maxAgePart = attributes.find((part) =>
    part.toLowerCase().startsWith("max-age="),
  );
  const maxAge = maxAgePart ? Number.parseInt(maxAgePart.split("=")[1], 10) : undefined;

  return {
    name,
    value,
    attributes: {
      maxAge: Number.isFinite(maxAge) ? (maxAge as number) : 60 * 60 * 12,
    },
  };
}

function applyCookieHeaders(
  response: NextResponse,
  cookieHeaders: string[],
  request: NextRequest,
) {
  for (const header of cookieHeaders) {
    const parsed = parseCookieHeader(header);
    if (!parsed) {
      continue;
    }

    if (
      parsed.name !== AUTH_COOKIE_NAME &&
      parsed.name !== REFRESH_COOKIE_NAME
    ) {
      continue;
    }

    const isExpiring = parsed.attributes.maxAge === 0;

    response.cookies.set({
      name: parsed.name,
      value: isExpiring ? "" : parsed.value,
      httpOnly: true,
      secure: request.nextUrl.protocol === "https:",
      path: "/",
      maxAge: isExpiring ? 0 : parsed.attributes.maxAge,
      sameSite: "lax",
    });
  }
}

function buildBackendCookieHeader(request: NextRequest): string | undefined {
  const authToken = request.cookies.get(AUTH_COOKIE_NAME)?.value;
  const refreshToken = request.cookies.get(REFRESH_COOKIE_NAME)?.value;
  const cookies = [
    authToken ? `${AUTH_COOKIE_NAME}=${authToken}` : null,
    refreshToken ? `${REFRESH_COOKIE_NAME}=${refreshToken}` : null,
  ].filter(Boolean);

  return cookies.length > 0 ? cookies.join("; ") : undefined;
}

async function proxyToBackend(
  request: NextRequest,
  pathStr: string,
  body?: string,
  cookieHeader?: string,
): Promise<Response> {
  const searchParams = request.nextUrl.searchParams.toString();
  const queryString = searchParams ? `?${searchParams}` : "";
  const headers: Record<string, string> = {};

  if (body !== undefined) {
    headers["Content-Type"] = "application/json";
  }

  if (cookieHeader) {
    headers.Cookie = cookieHeader;
  }

  return fetch(`${BACKEND_URL}/api/auth/${pathStr}${queryString}`, {
    method: request.method,
    headers,
    body,
  });
}

/**
 * BFF (Backend-For-Frontend) route handler for all /api/auth/* endpoints.
 *
 * Why this exists:
 * When Next.js rewrites are used to proxy requests to the Spring Boot backend,
 * Chrome blocks the Set-Cookie header from the backend response if the cookie
 * has a SameSite attribute and the response is considered "cross-site" by the
 * browser (even for same-origin fetch calls proxied via Turbopack dev server).
 *
 * This handler acts as an explicit proxy: it calls Spring Boot server-to-server,
 * reads the Set-Cookie from the backend response, and explicitly sets the cookie
 * on the Next.js response — from the correct origin (the app's own domain),
 * eliminating any SameSite cross-site ambiguity.
 */
async function handler(
  request: NextRequest,
  { params }: { params: Promise<{ path: string[] }> }
) {
  const { path } = await params;
  const pathStr = path.join("/");

  const body =
    request.method !== "GET" && request.method !== "HEAD"
      ? await request.text()
      : undefined;

  const incomingCookieHeader = buildBackendCookieHeader(request);
  let backendResponse = await proxyToBackend(
    request,
    pathStr,
    body,
    incomingCookieHeader,
  );

  let cookieHeaders = extractSetCookieHeaders(backendResponse);

  if (
    backendResponse.status === 401 &&
    pathStr === "me" &&
    request.cookies.get(REFRESH_COOKIE_NAME)?.value
  ) {
    const refreshResponse = await fetch(`${BACKEND_URL}/api/auth/refresh`, {
      method: "POST",
      headers: incomingCookieHeader ? { Cookie: incomingCookieHeader } : {},
    });

    const refreshCookieHeaders = extractSetCookieHeaders(refreshResponse);

    if (refreshResponse.ok) {
      const rotatedCookieHeader = refreshCookieHeaders
        .map((header) => parseCookieHeader(header))
        .filter(
          (
            parsed,
          ): parsed is { name: string; value: string; attributes: CookieAttributes } =>
            parsed !== null,
        )
        .map((parsed) => `${parsed.name}=${parsed.value}`)
        .join("; ");

      backendResponse = await proxyToBackend(
        request,
        pathStr,
        body,
        rotatedCookieHeader || incomingCookieHeader,
      );
      cookieHeaders = [...refreshCookieHeaders, ...extractSetCookieHeaders(backendResponse)];
    } else {
      cookieHeaders = refreshCookieHeaders;
    }
  }

  const responseBody =
    backendResponse.status === 204 ? null : await backendResponse.text();

  const response = new NextResponse(responseBody, {
    status: backendResponse.status,
    headers: { "Content-Type": "application/json" },
  });

  applyCookieHeaders(response, cookieHeaders, request);

  return response;
}

export const GET = handler;
export const POST = handler;
export const PUT = handler;
export const PATCH = handler;
export const DELETE = handler;
