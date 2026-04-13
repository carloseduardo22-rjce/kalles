package dev.kalles.sale.security.filter;

import com.auth0.jwt.interfaces.DecodedJWT;
import dev.kalles.sale.core.repository.CompanyRepository;
import dev.kalles.sale.security.context.CompanyContextHolder;
import dev.kalles.sale.security.context.PosContextHolder;
import dev.kalles.sale.security.context.TenantContextHolder;
import dev.kalles.sale.security.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
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
                    if (headerCompanyId != null && !headerCompanyId.isBlank()
                            && !tokenCompanyId.equals(UUID.fromString(headerCompanyId))) {
                        response.sendError(HttpServletResponse.SC_FORBIDDEN, "Company header conflicts with authenticated company");
                        return;
                    }
                    CompanyContextHolder.setCompanyId(tokenCompanyId);
                } else if (headerCompanyId != null && !headerCompanyId.trim().isEmpty()) {
                    UUID requestedCompanyId;
                    try {
                        requestedCompanyId = UUID.fromString(headerCompanyId);
                    } catch (IllegalArgumentException ex) {
                        response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid company header");
                        return;
                    }

                    if (!companyRepository.existsByIdAndTenantId(requestedCompanyId, tenantUuid)) {
                        response.sendError(HttpServletResponse.SC_FORBIDDEN, "Requested company is not accessible for authenticated tenant");
                        return;
                    }
                    CompanyContextHolder.setCompanyId(requestedCompanyId);
                }
                
                if (posId != null && !posId.trim().isEmpty()) {
                    PosContextHolder.setPosId(UUID.fromString(posId));
                }
            }
        }

        if (SecurityContextHolder.getContext().getAuthentication() != null
                && requiresCompanyContext(request)
                && CompanyContextHolder.getCompanyId() == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Company context is required for this endpoint");
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

    private boolean requiresCompanyContext(HttpServletRequest request) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return false;
        }
        String path = request.getRequestURI();
        return path.startsWith("/api/products")
                || path.startsWith("/api/warehouses")
                || path.startsWith("/api/stocks")
                || path.startsWith("/api/operators")
                || path.startsWith("/api/cash-registers")
                || path.startsWith("/api/cash-register-sessions")
                || path.startsWith("/api/sales")
                || path.startsWith("/api/clients")
                || path.startsWith("/api/fidelity")
                || path.startsWith("/api/fidelity-policies")
                || path.startsWith("/api/goals")
                || path.startsWith("/api/reports");
    }
}
