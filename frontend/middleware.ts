import { NextResponse } from "next/server";
import type { NextRequest } from "next/server";

export function middleware(request: NextRequest) {
  const accessToken = request.cookies.get("kalles_auth_token")?.value;
  const refreshToken = request.cookies.get("kalles_refresh_token")?.value;
  const hasSession = Boolean(accessToken || refreshToken);

  const currentPath = request.nextUrl.pathname;

  const isPublicPage =
    currentPath.startsWith("/login") ||
    currentPath.startsWith("/register") ||
    currentPath.startsWith("/verify");

  if (!hasSession && !isPublicPage) {
    return NextResponse.redirect(new URL("/login", request.url));
  }

  if (hasSession && isPublicPage) {
    return NextResponse.redirect(new URL("/caixas", request.url));
  }

  return NextResponse.next();
}

export const config = {
  matcher: [
    /*
     * Aplica o middleware em todas as rotas, EXCETO:
     * - /api/ (rotas de API internas do Next.js, se houver)
     * - /_next/static (arquivos estáticos)
     * - /_next/image (arquivos de imagem otimizados)
     * - extensões de arquivos públicos estáticos (favicon.ico, svg, png, etc)
     */
    "/((?!api|_next/static|_next/image|.*\\.png$|.*\\.svg$|.*\\.ico$).*)",
  ],
};
