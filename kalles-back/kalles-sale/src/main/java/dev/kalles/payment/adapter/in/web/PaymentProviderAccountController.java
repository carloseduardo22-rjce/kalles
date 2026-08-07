package dev.kalles.payment.adapter.in.web;

import dev.kalles.payment.adapter.in.web.dto.LinkPaymentProviderAccountRequest;
import dev.kalles.payment.adapter.in.web.dto.PaymentProviderAuthorizationUrlResponse;
import dev.kalles.payment.adapter.in.web.dto.PaymentProviderLinkStatusResponse;
import dev.kalles.payment.application.port.in.GetPaymentProviderAccountStatusUseCase;
import dev.kalles.payment.application.port.in.LinkPaymentProviderAccountUseCase;
import dev.kalles.payment.application.service.PaymentProviderOAuthStateService;
import dev.kalles.payment.domain.PaymentProvider;
import dev.kalles.security.context.TenantContextHolder;
import dev.kalles.security.domain.Account;
import dev.kalles.security.repository.AccountRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/payment-providers")
public class PaymentProviderAccountController {

    private final LinkPaymentProviderAccountUseCase linkPaymentProviderAccountUseCase;
    private final GetPaymentProviderAccountStatusUseCase getPaymentProviderAccountStatusUseCase;
    private final PaymentProviderOAuthStateService paymentProviderOAuthStateService;
    private final AccountRepository accountRepository;

    @Value("${mercadopago.app-id:448684586415948}")
    private String mercadoPagoAppId;

    @Value("${mercadopago.redirect-uri:https://localhost:3000/admin/pagamentos/mp-callback}")
    private String mercadoPagoRedirectUri;

    public PaymentProviderAccountController(
            LinkPaymentProviderAccountUseCase linkPaymentProviderAccountUseCase,
            GetPaymentProviderAccountStatusUseCase getPaymentProviderAccountStatusUseCase,
            PaymentProviderOAuthStateService paymentProviderOAuthStateService,
            AccountRepository accountRepository
    ) {
        this.linkPaymentProviderAccountUseCase = linkPaymentProviderAccountUseCase;
        this.getPaymentProviderAccountStatusUseCase = getPaymentProviderAccountStatusUseCase;
        this.paymentProviderOAuthStateService = paymentProviderOAuthStateService;
        this.accountRepository = accountRepository;
    }

    @GetMapping("/{provider}/oauth-authorization-url")
    public ResponseEntity<PaymentProviderAuthorizationUrlResponse> getAuthorizationUrl(
            @PathVariable PaymentProvider provider,
            Authentication authentication
    ) {
        Account account = resolveAuthenticatedAccount(authentication);
        String state = paymentProviderOAuthStateService.generateState(provider, account);
        ResponseCookie stateCookie = paymentProviderOAuthStateService.buildStateCookie(provider, state);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, stateCookie.toString())
                .body(new PaymentProviderAuthorizationUrlResponse(provider, buildAuthorizationUrl(provider, state)));
    }

    @PostMapping("/link")
    public ResponseEntity<Void> linkAccount(
            @Valid @RequestBody LinkPaymentProviderAccountRequest request,
            Authentication authentication,
            HttpServletRequest httpServletRequest
    ) {
        Account account = resolveAuthenticatedAccount(authentication);
        String expectedState = extractCookieValue(
                httpServletRequest,
                paymentProviderOAuthStateService.cookieName(request.provider())
        );

        linkPaymentProviderAccountUseCase.execute(request.toCommand(
                paymentProviderOAuthStateService.validateState(
                        request.provider(),
                        account,
                        request.state(),
                        expectedState
                )
        ));

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.SET_COOKIE,
                        paymentProviderOAuthStateService.buildExpiredStateCookie(request.provider()).toString()
                )
                .build();
    }

    @GetMapping("/{provider}/status")
    public ResponseEntity<PaymentProviderLinkStatusResponse> getStatus(@PathVariable PaymentProvider provider) {
        return ResponseEntity.ok(
                new PaymentProviderLinkStatusResponse(provider, getPaymentProviderAccountStatusUseCase.execute(provider))
        );
    }

    private Account resolveAuthenticatedAccount(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new IllegalArgumentException("Conta autenticada nao encontrada.");
        }

        return accountRepository.findByTenantIdAndEmailIgnoreCase(TenantContextHolder.getTenantId(), authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("Conta autenticada nao encontrada."));
    }

    private String buildAuthorizationUrl(PaymentProvider provider, String state) {
        if (provider != PaymentProvider.MERCADO_PAGO) {
            throw new IllegalArgumentException("Provider nao suporta onboarding OAuth.");
        }

        return "https://auth.mercadopago.com/authorization?client_id="
                + URLEncoder.encode(mercadoPagoAppId, StandardCharsets.UTF_8)
                + "&response_type=code&platform_id=mp&state="
                + URLEncoder.encode(state, StandardCharsets.UTF_8)
                + "&redirect_uri="
                + URLEncoder.encode(mercadoPagoRedirectUri, StandardCharsets.UTF_8);
    }

    private String extractCookieValue(HttpServletRequest request, String cookieName) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }

        for (Cookie cookie : cookies) {
            if (cookieName.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }

        return null;
    }
}
