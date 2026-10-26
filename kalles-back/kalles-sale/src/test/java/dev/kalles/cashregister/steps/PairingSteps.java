package dev.kalles.cashregister.steps;

import dev.kalles.security.entity.Account;
import dev.kalles.security.enums.AccountRole;
import dev.kalles.testsupport.CsrfTestClient;
import dev.kalles.testsupport.CsrfTestClient.CsrfContext;
import dev.kalles.testsupport.LocalHttpTestClient;
import io.cucumber.java.Before;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Entao;
import io.cucumber.java.pt.Quando;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

public class PairingSteps extends CashRegisterCucumberSpringConfiguration {

    private static final String ADMIN_EMAIL = "admin.caixa@sistema.local";
    private static final String DEVICE_COOKIE = "kalles_pos_token";

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private String adminAuthCookie;
    private CsrfContext csrf;
    private String pairingCode;
    private final Map<String, String> devices = new HashMap<>();
    private final Map<String, String> openSessions = new HashMap<>();
    private Response response;

    @Before("@RF-002")
    public void beforePairingScenario() {
        resetScenarioData();
        devices.clear();
        openSessions.clear();
        pairingCode = null;
        response = null;
    }

    @Dado("um administrador autenticado na loja do caixa")
    public void givenAuthenticatedAdmin() {
        Account admin = new Account(TENANT_ID, "Administrador do caixa", ADMIN_EMAIL,
                passwordEncoder.encode(DEFAULT_PASSWORD), AccountRole.ADMIN);
        admin.setVerified(true);
        accountRepository.save(admin);

        Response login = given()
                .contentType(ContentType.JSON)
                .body(Map.of("email", ADMIN_EMAIL, "password", DEFAULT_PASSWORD))
                .when()
                .post("/api/auth/login");
        login.then().statusCode(200);
        adminAuthCookie = login.getCookie("kalles_auth_token");
        csrf = CsrfTestClient.fetch(port);
    }

    @Quando("o administrador gerar o codigo de pareamento do caixa")
    public void whenAdminGeneratesPairingCode() {
        response = adminRequest().post("/api/cash-registers/{id}/pairing-code", cashRegisterId);
    }

    @Dado("um codigo de pareamento gerado para o caixa")
    public void givenGeneratedPairingCode() {
        whenAdminGeneratesPairingCode();
        assertThat(response.statusCode()).isEqualTo(201);
        pairingCode = response.jsonPath().getString("code");
    }

    @Dado("um codigo de pareamento ja usado")
    public void givenUsedPairingCode() {
        givenGeneratedPairingCode();
        whenDeviceSubmitsPairingCode("A");
        assertThat(response.statusCode()).isEqualTo(200);
    }

    @Dado("um codigo de pareamento vencido")
    public void givenExpiredPairingCode() {
        givenGeneratedPairingCode();
        jdbcTemplate.update(
                "UPDATE pos_pairing_code SET expires_at = ? WHERE cash_register_id = ?",
                LocalDateTime.now().minusMinutes(1), cashRegisterId);
    }

    @Dado("um codigo de pareamento inexistente")
    public void givenUnknownPairingCode() {
        pairingCode = "ZZZZ2222";
    }

    @Quando("o dispositivo {string} informar o codigo de pareamento")
    public void whenDeviceSubmitsPairingCode(String device) {
        response = given()
                .contentType(ContentType.JSON)
                .cookie("XSRF-TOKEN", csrf.csrfCookie())
                .header("X-XSRF-TOKEN", csrf.csrfToken())
                .body(Map.of("pairingCode", pairingCode))
                .when()
                .post("/api/pos/setup");
        String deviceToken = response.getCookie(DEVICE_COOKIE);
        if (deviceToken != null) {
            devices.put(device, deviceToken);
        }
    }

    @Dado("o dispositivo {string} pareado com o caixa")
    public void givenPairedDevice(String device) {
        givenGeneratedPairingCode();
        whenDeviceSubmitsPairingCode(device);
        assertThat(response.statusCode()).isEqualTo(200);
    }

    @Dado("o dispositivo {string} pareado com o caixa ha {int} dias")
    public void givenDevicePairedDaysAgo(String device, int days) {
        givenPairedDevice(device);
        jdbcTemplate.update(
                "UPDATE pos_device_session SET created_at = ? WHERE pos_id = ? AND active",
                LocalDateTime.now().minusDays(days), cashRegisterId);
    }

    @Dado("o dispositivo {string} com a sessao do caixa aberta")
    public void givenDeviceWithOpenSession(String device) {
        thenDeviceCanOpenSession(device);
        openSessions.put(device, response.jsonPath().getString("sessionId"));
    }

    @Quando("o dispositivo {string} tentar abrir a sessao do caixa")
    public void whenDeviceTriesToOpenSession(String device) {
        Map<String, Object> body = new HashMap<>();
        body.put("cashRegisterCode", CASH_REGISTER_CODE);
        body.put("operatorCode", OPERATOR_CODE);
        body.put("initialAmount", 100);
        body.put("allowCashOnlyOperation", true);

        response = deviceRequest(device).body(body).post("/api/cash-register-sessions/open");
    }

    @Quando("o administrador revogar o pareamento do caixa")
    public void whenAdminRevokesPairing() {
        response = LocalHttpTestClient.delete(
                "http://localhost:" + port + "/api/cash-registers/" + cashRegisterId + "/pairing",
                adminHeaders(null));
    }

    @Entao("a resposta do pareamento deve ter status HTTP {int}")
    public void thenPairingResponseStatus(int status) {
        assertThat(response).isNotNull();
        assertThat(response.statusCode()).isEqualTo(status);
    }

    @Entao("o codigo gerado deve ter {int} caracteres")
    public void thenGeneratedCodeLength(int length) {
        assertThat(response.jsonPath().getString("code")).hasSize(length);
    }

    @Entao("o codigo gerado deve vencer em {int} minutos")
    public void thenGeneratedCodeExpiresIn(int minutes) {
        LocalDateTime expiresAt = LocalDateTime.parse(response.jsonPath().getString("expiresAt"));
        assertThat(Duration.between(LocalDateTime.now(), expiresAt).toSeconds())
                .isCloseTo(minutes * 60L, within(60L));
    }

    @Entao("o erro do pareamento deve ter o codigo {string}")
    public void thenPairingErrorCode(String code) {
        assertThat(response.jsonPath().getString("code")).isEqualTo(code);
    }

    @Entao("o dispositivo {string} deve estar pareado com o caixa")
    public void thenDeviceIsPaired(String device) {
        Response deviceResponse = LocalHttpTestClient.get(
                "http://localhost:" + port + "/api/pos/device", adminHeaders(device));
        assertThat(deviceResponse.statusCode()).isEqualTo(200);
        assertThat(deviceResponse.jsonPath().getString("cashRegisterId")).isEqualTo(cashRegisterId.toString());
        assertThat(deviceResponse.jsonPath().getString("cashRegisterCode")).isEqualTo(CASH_REGISTER_CODE);
    }

    @Entao("o dispositivo {string} deve conseguir abrir a sessao do caixa")
    public void thenDeviceCanOpenSession(String device) {
        whenDeviceTriesToOpenSession(device);
        assertThat(response.statusCode()).isEqualTo(201);
    }

    @Entao("o dispositivo {string} nao deve conseguir abrir a sessao do caixa")
    public void thenDeviceCannotOpenSession(String device) {
        whenDeviceTriesToOpenSession(device);
        assertThat(response.statusCode()).isEqualTo(403);
        assertThat(response.jsonPath().getString("code")).isEqualTo("DEVICE_NOT_PAIRED");
    }

    @Entao("o dispositivo {string} nao deve conseguir registrar venda na sessao aberta")
    public void thenDeviceCannotOperateSession(String device) {
        Response sale = deviceRequest(device).post("/api/sales/{sessionToken}", openSessions.get(device));
        assertThat(sale.statusCode()).isEqualTo(403);
        assertThat(sale.jsonPath().getString("code")).isEqualTo("DEVICE_NOT_PAIRED");
    }

    private RequestSpecification adminRequest() {
        return given()
                .contentType(ContentType.JSON)
                .cookie("kalles_auth_token", adminAuthCookie)
                .cookie("XSRF-TOKEN", csrf.csrfCookie())
                .header("X-XSRF-TOKEN", csrf.csrfToken())
                .header("X-Company-ID", companyId.toString())
                .when();
    }

    private RequestSpecification deviceRequest(String device) {
        RequestSpecification request = adminRequest();
        String deviceToken = devices.get(device);
        if (deviceToken != null) {
            request.cookie(DEVICE_COOKIE, deviceToken);
        }
        return request;
    }

    private Map<String, String> adminHeaders(String device) {
        StringBuilder cookies = new StringBuilder("kalles_auth_token=" + adminAuthCookie
                + "; XSRF-TOKEN=" + csrf.csrfCookie());
        if (device != null && devices.containsKey(device)) {
            cookies.append("; ").append(DEVICE_COOKIE).append("=").append(devices.get(device));
        }
        return Map.of(
                "Cookie", cookies.toString(),
                "X-XSRF-TOKEN", csrf.csrfToken(),
                "X-Company-ID", companyId.toString());
    }
}
