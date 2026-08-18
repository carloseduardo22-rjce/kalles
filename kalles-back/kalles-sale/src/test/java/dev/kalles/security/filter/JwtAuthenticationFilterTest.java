package dev.kalles.security.filter;

import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import dev.kalles.company.repository.CompanyRepository;
import dev.kalles.security.context.CompanyContextHolder;
import dev.kalles.security.context.PosContextHolder;
import dev.kalles.security.context.TenantContextHolder;
import dev.kalles.security.exception.ProblemResponseWriter;
import dev.kalles.security.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("JwtAuthenticationFilter")
class JwtAuthenticationFilterTest {

    private static final String TOKEN = "token-valido";

    @Mock
    private JwtService jwtService;

    @Mock
    private CompanyRepository companyRepository;

    private JwtAuthenticationFilter filter;

    @BeforeEach
    void setUp() {
        filter = new JwtAuthenticationFilter(
                jwtService,
                companyRepository,
                new ProblemResponseWriter(JsonMapper.builder().build())
        );
    }

    @AfterEach
    void tearDown() {
        TenantContextHolder.clear();
        CompanyContextHolder.clear();
        PosContextHolder.clear();
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("deve limpar o contexto quando o header de filial nao e um uuid")
    void shouldClearContextWhenCompanyHeaderIsNotAUuid() throws ServletException, IOException {
        DecodedJWT decodedJWT = decodedToken(UUID.randomUUID(), UUID.randomUUID(), null);
        when(jwtService.validateToken(TOKEN)).thenReturn(decodedJWT);

        MockHttpServletRequest request = authenticatedRequest("/api/sales");
        request.addHeader(JwtAuthenticationFilter.COMPANY_HEADER_NAME, "nao-e-um-uuid");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, new MockFilterChain());

        assertEquals(400, response.getStatus());
        assertContextIsClear();
    }

    @Test
    @DisplayName("deve limpar o contexto quando o header de filial conflita com o token")
    void shouldClearContextWhenCompanyHeaderConflictsWithToken() throws ServletException, IOException {
        DecodedJWT decodedJWT = decodedToken(UUID.randomUUID(), UUID.randomUUID(), null);
        when(jwtService.validateToken(TOKEN)).thenReturn(decodedJWT);

        MockHttpServletRequest request = authenticatedRequest("/api/sales");
        request.addHeader(JwtAuthenticationFilter.COMPANY_HEADER_NAME, UUID.randomUUID().toString());
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, new MockFilterChain());

        assertEquals(403, response.getStatus());
        assertContextIsClear();
    }

    @Test
    @DisplayName("deve limpar o contexto quando a rota exige filial e o token nao traz nenhuma")
    void shouldClearContextWhenRouteRequiresCompanyAndTokenHasNone() throws ServletException, IOException {
        DecodedJWT decodedJWT = decodedToken(UUID.randomUUID(), null, null);
        when(jwtService.validateToken(TOKEN)).thenReturn(decodedJWT);

        MockHttpServletRequest request = authenticatedRequest("/api/sales");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, new MockFilterChain());

        assertEquals(400, response.getStatus());
        assertContextIsClear();
    }

    @Test
    @DisplayName("nao deve vazar o tenant para a requisicao seguinte na mesma thread")
    void shouldNotLeakTenantIntoNextRequestOnSameThread() throws ServletException, IOException {
        UUID tenantId = UUID.randomUUID();
        DecodedJWT decodedJWT = decodedToken(tenantId, UUID.randomUUID(), null);
        when(jwtService.validateToken(TOKEN)).thenReturn(decodedJWT);

        MockHttpServletRequest rejected = authenticatedRequest("/api/sales");
        rejected.addHeader(JwtAuthenticationFilter.COMPANY_HEADER_NAME, "nao-e-um-uuid");
        filter.doFilter(rejected, new MockHttpServletResponse(), new MockFilterChain());

        SecurityContextHolder.clearContext();

        AtomicReference<UUID> tenantSeenByChain = new AtomicReference<>();
        FilterChain capturingChain = (req, res) -> tenantSeenByChain.set(TenantContextHolder.getTenantId());

        MockHttpServletRequest anonymous = new MockHttpServletRequest("POST", "/api/pos/setup");
        anonymous.setRequestURI("/api/pos/setup");
        filter.doFilter(anonymous, new MockHttpServletResponse(), capturingChain);

        assertNull(tenantSeenByChain.get());
    }

    private void assertContextIsClear() {
        assertNull(TenantContextHolder.getTenantId());
        assertNull(CompanyContextHolder.getCompanyId());
        assertNull(PosContextHolder.getPosId());
    }

    private MockHttpServletRequest authenticatedRequest(String uri) {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", uri);
        request.setRequestURI(uri);
        request.setCookies(new Cookie(JwtAuthenticationFilter.AUTH_COOKIE_NAME, TOKEN));
        return request;
    }

    private DecodedJWT decodedToken(UUID tenantId, UUID companyId, UUID posId) {
        Claim tenantClaim = claim(tenantId);
        Claim roleClaim = claim("ADMIN");
        Claim companyClaim = claim(companyId);
        Claim posClaim = claim(posId);

        DecodedJWT decodedJWT = mock(DecodedJWT.class);
        when(decodedJWT.getSubject()).thenReturn("usuario@kalles.local");
        when(decodedJWT.getClaim("tenantId")).thenReturn(tenantClaim);
        when(decodedJWT.getClaim("role")).thenReturn(roleClaim);
        when(decodedJWT.getClaim("companyId")).thenReturn(companyClaim);
        when(decodedJWT.getClaim("posId")).thenReturn(posClaim);
        return decodedJWT;
    }

    private Claim claim(Object value) {
        Claim claim = mock(Claim.class);
        when(claim.asString()).thenReturn(value == null ? null : value.toString());
        return claim;
    }
}
