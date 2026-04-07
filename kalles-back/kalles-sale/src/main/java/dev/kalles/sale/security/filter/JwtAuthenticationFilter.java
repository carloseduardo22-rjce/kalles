package dev.kalles.sale.security.filter;

import com.auth0.jwt.interfaces.DecodedJWT;
import dev.kalles.sale.security.context.CompanyContextHolder;
import dev.kalles.sale.security.context.PosContextHolder;
import dev.kalles.sale.security.context.TenantContextHolder;
import dev.kalles.sale.security.domain.AccountRole;
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
    public static final String AUTH_COOKIE_NAME = "kalles_auth_token";

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

                // Set Spring Security Context
                var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role));
                var authentication = new UsernamePasswordAuthenticationToken(login, null, authorities);
                SecurityContextHolder.getContext().setAuthentication(authentication);

                // Set Tenant Context
                TenantContextHolder.setTenantId(UUID.fromString(tenantId));
                
                // Set Specific Store Context if present
                if (companyId != null && !companyId.trim().isEmpty()) {
                    CompanyContextHolder.setCompanyId(UUID.fromString(companyId));
                } else if ("ADMIN".equals(role)) {
                    String headerCompanyId = request.getHeader("x-company-id");
                    if (headerCompanyId != null && !headerCompanyId.trim().isEmpty()) {
                        CompanyContextHolder.setCompanyId(UUID.fromString(headerCompanyId));
                    }
                }
                
                if (posId != null && !posId.trim().isEmpty()) {
                    PosContextHolder.setPosId(UUID.fromString(posId));
                }
            }
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
}