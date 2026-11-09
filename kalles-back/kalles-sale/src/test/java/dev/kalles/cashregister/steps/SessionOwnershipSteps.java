package dev.kalles.cashregister.steps;

import dev.kalles.security.entity.Account;
import dev.kalles.security.enums.AccountRole;
import io.cucumber.java.Before;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.E;
import io.cucumber.java.pt.Entao;
import io.cucumber.java.pt.Quando;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

public class SessionOwnershipSteps extends CashRegisterCucumberSpringConfiguration {

    private static final String ADMIN_EMAIL = "admin.loja@sistema.local";
    private static final String UNLINKED_EMAIL = "sem.cadastro@sistema.local";

    private AuthContext authContext;
    private Response response;

    @Before(value = "@contaDaSessao", order = 20000)
    public void beforeScenario() {
        resetScenarioData();
        authContext = null;
        response = null;
    }

    @Dado("um operador com cadastro autenticado no dispositivo pareado")
    public void givenLinkedOperator() {
        authContext = authenticateOperator();
    }

    @Dado("um administrador sem cadastro de operador autenticado no dispositivo pareado")
    public void givenAdministratorWithoutOperator() {
        seedAccount("Administrador da loja", ADMIN_EMAIL, AccountRole.ADMIN, null);
        authContext = authenticateOnPairedDevice(ADMIN_EMAIL);
    }

    @Dado("uma conta de operador sem cadastro autenticada no dispositivo pareado")
    public void givenOperatorAccountWithoutOperator() {
        seedAccount("Sem cadastro", UNLINKED_EMAIL, AccountRole.OPERATOR, companyId);
        authContext = authenticateOnPairedDevice(UNLINKED_EMAIL);
    }

    @E("o operador ja tem sessao aberta em outro caixa")
    public void givenOperatorBusyOnAnotherCashRegister() {
        seedCashRegister(companyId, "CAIXA-02", "Caixa secundario");
        seedSession(companyId, "CAIXA-02", OPERATOR_CODE, BigDecimal.TEN);
    }

    @Quando("a pessoa autenticada abrir a sessao do caixa")
    public void whenOpeningSession() {
        response = given()
                .contentType(ContentType.JSON)
                .cookie("kalles_auth_token", authContext.authCookie())
                .cookie("XSRF-TOKEN", authContext.csrfCookie())
                .header("X-XSRF-TOKEN", authContext.csrfToken())
                .header("X-Company-ID", companyId.toString())
                .body(Map.of(
                        "cashRegisterCode", CASH_REGISTER_CODE,
                        "initialAmount", 100,
                        "allowCashOnlyOperation", true
                ))
                .when()
                .post("/api/cash-register-sessions/open");
    }

    @Entao("a abertura pela conta deve ter status HTTP {int}")
    public void thenStatus(int status) {
        assertThat(response.statusCode()).isEqualTo(status);
    }

    @Entao("a sessao deve estar em nome de {string}")
    public void thenSessionBelongsTo(String name) {
        assertThat(response.jsonPath().getString("operatorName")).isEqualTo(name);
    }

    @Entao("o administrador deve ter recebido a matricula {string}")
    public void thenAdministratorReceivedCode(String code) {
        UUID operatorId = UUID.fromString(response.jsonPath().getString("operatorId"));
        assertThat(operatorRepository.findById(operatorId).orElseThrow().getCode()).isEqualTo(code);
    }

    @Entao("o erro da abertura deve ter o codigo {string}")
    public void thenErrorCode(String code) {
        assertThat(response.jsonPath().getString("code")).isEqualTo(code);
    }

    private void seedAccount(String name, String email, AccountRole role, UUID accountCompanyId) {
        Account account = new Account(TENANT_ID, name, email, passwordEncoder.encode(DEFAULT_PASSWORD), role);
        account.setCompanyId(accountCompanyId);
        account.setVerified(true);
        accountRepository.save(account);
    }
}
