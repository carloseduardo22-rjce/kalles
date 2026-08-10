package dev.kalles.security.config;

import dev.kalles.security.filter.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.InvalidCsrfTokenException;
import org.springframework.security.web.csrf.MissingCsrfTokenException;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Value("${cors.allowed-origin:http://localhost:3000}")
    private String allowedOrigin;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                .ignoringRequestMatchers(
                        "/api/auth/login",
                        "/api/auth/register",
                        "/api/auth/verify",
                        "/api/auth/resend-code",
                        "/api/auth/refresh",
                        "/api/auth/logout",
                        "/api/pos/setup",
                        "/api/billing/webhook",
                        "/api/webhooks/mercadopago"
                )
            )
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(exceptions -> exceptions.accessDeniedHandler(accessDeniedHandler()))
            .authorizeHttpRequests(authorize -> authorize
                // ── Public endpoints ──
                .requestMatchers(HttpMethod.GET, "/api/auth/csrf").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/auth/login", "/api/auth/register", "/api/auth/verify", "/api/auth/resend-code", "/api/auth/refresh", "/api/auth/logout", "/api/pos/setup").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/billing/webhook").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/webhooks/mercadopago").permitAll()
                .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                .requestMatchers("/error").permitAll()

                // ── PDV operations: OPERATOR + ADMIN ──
                .requestMatchers("/api/sales/**").hasAnyRole("ADMIN", "OPERATOR")
                .requestMatchers("/api/cash-register-sessions/**").hasAnyRole("ADMIN", "OPERATOR")
                .requestMatchers(HttpMethod.POST, "/api/payments/process").hasAnyRole("ADMIN", "OPERATOR")
                .requestMatchers(HttpMethod.GET, "/api/payment-terminal-mappings/by-cash-register").hasAnyRole("ADMIN", "OPERATOR")

                // OPERATOR needs to search/view products in PDV
                .requestMatchers(HttpMethod.GET, "/api/products/search").hasAnyRole("ADMIN", "OPERATOR")
                .requestMatchers(HttpMethod.GET, "/api/products/{id}").hasAnyRole("ADMIN", "OPERATOR")

                // OPERATOR needs to read clients for fidelity
                .requestMatchers(HttpMethod.GET, "/api/clients/**").hasAnyRole("ADMIN", "OPERATOR")

                // OPERATOR needs fidelity read + enroll
                .requestMatchers(HttpMethod.GET, "/api/fidelity/**").hasAnyRole("ADMIN", "OPERATOR")
                .requestMatchers(HttpMethod.POST, "/api/fidelity/enroll/**").hasAnyRole("ADMIN", "OPERATOR")
                .requestMatchers("/api/tickets/**").hasAnyRole("ADMIN", "OPERATOR")
                .requestMatchers(HttpMethod.GET, "/api/categories/**").hasAnyRole("ADMIN", "OPERATOR")
                .requestMatchers("/api/notes/sensitive/**").hasAnyRole("ADMIN", "OPERATOR")

                // ── Admin-only: management CRUD ──
                .requestMatchers("/api/agents/**").hasRole("ADMIN")
                .requestMatchers("/api/users/**").hasRole("ADMIN")
                .requestMatchers("/api/products/**").hasRole("ADMIN")
                .requestMatchers("/api/warehouses/**").hasRole("ADMIN")
                .requestMatchers("/api/stocks/**").hasRole("ADMIN")
                .requestMatchers("/api/operators/**").hasRole("ADMIN")
                .requestMatchers("/api/cash-registers/**").hasRole("ADMIN")
                .requestMatchers("/api/goals/**").hasRole("ADMIN")
                .requestMatchers("/api/fidelity-policies/**").hasRole("ADMIN")
                .requestMatchers("/api/reports/**").hasRole("ADMIN")
                .requestMatchers("/api/companies/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/categories/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/categories/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/categories/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/clients/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/clients/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/clients/**").hasRole("ADMIN")

                // ── Payment/integrations: ADMIN only ──
                .requestMatchers("/api/payment/**").hasRole("ADMIN")
                .requestMatchers("/api/payments/**").hasRole("ADMIN")
                .requestMatchers("/api/payment-points/**").hasRole("ADMIN")
                .requestMatchers("/api/payment-providers/**").hasRole("ADMIN")
                .requestMatchers("/api/payment-stores/**").hasRole("ADMIN")
                .requestMatchers("/api/payment-terminals/**").hasRole("ADMIN")
                .requestMatchers("/api/payment-terminal-mappings/**").hasRole("ADMIN")

                // ── Fallback ──
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return (request, response, accessDeniedException) -> {
            String code = "ACCESS_DENIED";
            String title = "Acesso negado";
            String detail = "Usuario autenticado nao possui permissao para executar esta operacao.";

            if (accessDeniedException instanceof MissingCsrfTokenException) {
                code = "CSRF_MISSING";
                title = "Token CSRF ausente";
                detail = "A requisicao exige token CSRF. Carregue /api/auth/csrf e envie o token no header X-XSRF-TOKEN.";
            } else if (accessDeniedException instanceof InvalidCsrfTokenException) {
                code = "CSRF_INVALID";
                title = "Token CSRF invalido";
                detail = "O token CSRF enviado nao confere com o token esperado para esta sessao.";
            }

            response.setStatus(403);
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.setContentType("application/problem+json");
            response.getWriter().write("""
                    {"type":"about:blank","title":"%s","status":403,"detail":"%s","code":"%s","path":"%s"}"""
                    .formatted(escapeJson(title), escapeJson(detail), escapeJson(code), escapeJson(request.getRequestURI())));
        };
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of(allowedOrigin));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS", "HEAD"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "Accept", "X-Requested-With", "X-Company-ID", "x-company-id", "X-XSRF-TOKEN"));
        configuration.setExposedHeaders(List.of("X-Kalles-Company-Context-Error"));
        configuration.setAllowCredentials(true); // Super important for cookies!

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    private static String escapeJson(String value) {
        return value == null ? "" : value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }
}
