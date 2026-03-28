import { NextRequest, NextResponse } from "next/server";

const BACKEND_URL =
  process.env.BACKEND_URL ?? "http://localhost:8080";

const COOKIE_NAME = "kalles_auth_token";

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

  const searchParams = request.nextUrl.searchParams.toString();
  const queryString = searchParams ? `?${searchParams}` : "";

  const backendResponse = await fetch(
    `${BACKEND_URL}/api/auth/${pathStr}${queryString}`,
    {
      method: request.method,
      headers: { "Content-Type": "application/json" },
      body,
    }
  );

  const responseBody =
    backendResponse.status === 204 ? null : await backendResponse.text();

  const setCookieHeader = backendResponse.headers.get("set-cookie");

  const response = new NextResponse(responseBody, {
    status: backendResponse.status,
    headers: { "Content-Type": "application/json" },
  });

  if (setCookieHeader) {
    const parts = setCookieHeader.split(";").map((p) => p.trim());
    const [cookieName, cookieValue] = parts[0].split("=");

    if (cookieName.trim() === COOKIE_NAME) {
      const maxAgeStr = parts
        .find((p) => p.toLowerCase().startsWith("max-age="))
        ?.split("=")?.[1];
      const maxAge = maxAgeStr ? parseInt(maxAgeStr, 10) : 60 * 60 * 12;
      const isExpiring = maxAge === 0;

      response.cookies.set({
        name: COOKIE_NAME,
        value: isExpiring ? "" : cookieValue.trim(),
        httpOnly: true,
        secure: request.nextUrl.protocol === "https:",
        path: "/",
        maxAge: isExpiring ? 0 : maxAge,
        sameSite: "lax",
      });
    }
  }

  return response;
}

export const GET = handler;
export const POST = handler;
export const PUT = handler;
export const PATCH = handler;
export const DELETE = handler;
