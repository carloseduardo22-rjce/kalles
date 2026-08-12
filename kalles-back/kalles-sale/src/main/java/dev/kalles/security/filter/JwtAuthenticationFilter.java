package dev.kalles.security.filter;

import com.auth0.jwt.interfaces.DecodedJWT;
import dev.kalles.company.repository.CompanyRepository;
import dev.kalles.security.context.CompanyContextHolder;
import dev.kalles.security.context.PosContextHolder;
import dev.kalles.security.context.TenantContextHolder;
import dev.kalles.security.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final CompanyRepository companyRepository;
    public static final String AUTH_COOKIE_NAME = "kalles_auth_token";
    public static final String COMPANY_HEADER_NAME = "x-company-id";
    public static final String COMPANY_CONTEXT_ERROR_HEADER = "X-Kalles-Company-Context-Error";

    private static final List<String> COMPANY_AGNOSTIC_PREFIXES = List.of(
            "/api/auth",
            "/api/pos",
            "/api/companies",
            "/api/billing",
            "/api/webhooks",
            "/api/users",
            "/api/agents",
            "/api/categories",
            "/api/notes",
            "/api/payment-providers",
            "/api/payment-points",
            "/api/payment-stores",
            "/api/payment-terminals"
    );

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, 
                                    @NonNull HttpServletResponse response, 
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        
        var token = this.recoverToken(request);
        if (token != null) {
            DecodedJWT decodedJWT = jwtService.validateToken(token);
            if(decodedJWT != null) {
                var login = decodedJWT.getSubject();
                var tenantId = decodedJWT.getClaim("tenantId").asString();
                var role = decodedJWT.getClaim("role").asString();
                
                var companyId = decodedJWT.getClaim("companyId").asString();
                var posId = decodedJWT.getClaim("posId").asString();
                UUID tenantUuid = UUID.fromString(tenantId);

                // Set Spring Security Context
                var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role));
                var authentication = new UsernamePasswordAuthenticationToken(login, null, authorities);
                SecurityContextHolder.getContext().setAuthentication(authentication);

                // Set Tenant Context
                TenantContextHolder.setTenantId(tenantUuid);
                
                // Set Specific Store Context if present
                String headerCompanyId = request.getHeader(COMPANY_HEADER_NAME);
                if (companyId != null && !companyId.trim().isEmpty()) {
                    UUID tokenCompanyId = UUID.fromString(companyId);
                    if (headerCompanyId != null && !headerCompanyId.isBlank()) {
                        UUID requestedCompanyId = parseCompanyHeader(headerCompanyId, response);
                        if (requestedCompanyId == null) {
                            return;
                        }
                        if (!tokenCompanyId.equals(requestedCompanyId)) {
                            sendCompanyContextForbidden(response, "Company header conflicts with authenticated company");
                            return;
                        }
                    }
                    CompanyContextHolder.setCompanyId(tokenCompanyId);
                } else if (headerCompanyId != null && !headerCompanyId.trim().isEmpty()) {
                    UUID requestedCompanyId = parseCompanyHeader(headerCompanyId, response);
                    if (requestedCompanyId == null) {
                        return;
                    }

                    if (!companyRepository.existsByIdAndTenantId(requestedCompanyId, tenantUuid)) {
                        sendCompanyContextForbidden(response, "Requested company is not accessible for authenticated tenant");
                        return;
                    }
                    CompanyContextHolder.setCompanyId(requestedCompanyId);
                }
                
                if (posId != null && !posId.trim().isEmpty()) {
                    PosContextHolder.setPosId(UUID.fromString(posId));
                }
            }
        }

        if (isAuthenticated()
                && requiresCompanyContext(request)
                && CompanyContextHolder.getCompanyId() == null) {
            writeProblem(response, HttpServletResponse.SC_BAD_REQUEST, "COMPANY_CONTEXT_REQUIRED",
                    "Contexto de filial obrigatorio",
                    "Esta rota exige uma filial ativa. Envie X-Company-ID com uma filial acessivel para o tenant autenticado.");
            return;
        }
        
        try {
            filterChain.doFilter(request, response);
        } finally {
            // Guarantee cleanup to prevent data leaking into another thread
            TenantContextHolder.clear();
            CompanyContextHolder.clear();
            PosContextHolder.clear();
        }
    }

    private String recoverToken(HttpServletRequest request) {
        if (request.getCookies() != null) {
            return Arrays.stream(request.getCookies())
                    .filter(c -> AUTH_COOKIE_NAME.equals(c.getName()))
                    .map(Cookie::getValue)
                    .findFirst()
                    .orElse(null);
        }
        return null;
    }

    private UUID parseCompanyHeader(String headerCompanyId, HttpServletResponse response) throws IOException {
        try {
            return UUID.fromString(headerCompanyId);
        } catch (IllegalArgumentException ex) {
            writeProblem(response, HttpServletResponse.SC_BAD_REQUEST, "COMPANY_CONTEXT_INVALID",
                    "Header de filial invalido", "O header X-Company-ID deve conter um UUID valido.");
            return null;
        }
    }

    private void sendCompanyContextForbidden(HttpServletResponse response, String message) throws IOException {
        response.setHeader(COMPANY_CONTEXT_ERROR_HEADER, "true");
        writeProblem(response, HttpServletResponse.SC_FORBIDDEN, "COMPANY_CONTEXT_DENIED",
                "Contexto de filial negado", message);
    }

    private void writeProblem(HttpServletResponse response, int status, String code, String title, String detail) throws IOException {
        response.setStatus(status);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("application/problem+json");
        response.getWriter().write("""
                {"type":"about:blank","title":"%s","status":%d,"detail":"%s","code":"%s"}"""
                .formatted(escapeJson(title), status, escapeJson(detail), escapeJson(code)));
    }

    private String escapeJson(String value) {
        return value == null ? "" : value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }

    private boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken);
    }

    private boolean requiresCompanyContext(HttpServletRequest request) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return false;
        }
        String path = request.getRequestURI();
        return path.startsWith("/api/")
                && COMPANY_AGNOSTIC_PREFIXES.stream().noneMatch(path::startsWith);
    }
}
