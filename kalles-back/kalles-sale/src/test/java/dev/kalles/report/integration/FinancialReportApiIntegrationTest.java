package dev.kalles.report.integration;

import dev.kalles.report.support.AbstractFinancialReportApiSupport;
import dev.kalles.testsupport.LocalHttpTestClient;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Map;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

@Tag("integration")
class FinancialReportApiIntegrationTest extends AbstractFinancialReportApiSupport {

    @BeforeEach
    void setUp() {
        resetFinancialReportScenario();
    }

    @Test
    void shouldReturnFinancialReportForActiveCompanyOnly() {
        AuthContext auth = authenticateTenantAdminWithCsrf();

        var productA = seedProduct(TENANT_ID, "Arroz", "ARZ-001");
        var locationA = seedLocation(companyAId, "Deposito A", "A-01");
        seedStockEntry(companyAId, productA, locationA, 10, "20.00", LocalDateTime.of(2026, 4, 10, 10, 0));
        seedCompletedSale(companyAId, "CX-A1-RPT", "op-a1-rpt", "500.00", LocalDateTime.of(2026, 4, 10, 9, 0));

        var productB = seedProduct(TENANT_ID, "Feijao", "FEJ-001");
        var locationB = seedLocation(companyBId, "Deposito B", "B-01");
        seedStockEntry(companyBId, productB, locationB, 5, "30.00", LocalDateTime.of(2026, 4, 10, 10, 0));
        seedCompletedSale(companyBId, "CX-B1-RPT", "op-b1-rpt", "900.00", LocalDateTime.of(2026, 4, 10, 9, 0));

        Response response = LocalHttpTestClient.get(
                "http://localhost:" + port + "/api/reports/profit-vs-supplier-expenses?startDate=2026-04-01&endDate=2026-04-30",
                Map.of(
                        "Cookie", "kalles_auth_token=" + auth.authCookie(),
                        "X-Company-ID", companyAId.toString()
                )
        );

        response.then()
                .statusCode(200)
                .body("totalSales", equalTo(500.00f))
                .body("totalSupplierExpenses", equalTo(200.00f))
                .body("estimatedProfit", equalTo(300.00f))
                .body("marginPercentage", equalTo(60.00f))
                .body("purchasedProducts", hasSize(1))
                .body("purchasedProducts[0].productInternalCode", equalTo("ARZ-001"));
    }

    @Test
    void shouldRejectInvalidDateRange() {
        AuthContext auth = authenticateTenantAdminWithCsrf();

        Response response = LocalHttpTestClient.get(
                "http://localhost:" + port + "/api/reports/profit-vs-supplier-expenses?startDate=2026-04-30&endDate=2026-04-01",
                Map.of(
                        "Cookie", "kalles_auth_token=" + auth.authCookie(),
                        "X-Company-ID", companyAId.toString()
                )
        );

        response.then()
                .statusCode(400)
                .body("detail", equalTo("A data final nao pode ser menor que a data inicial."));
    }

    @Test
    void shouldRequireActiveCompanyForFinancialReportRoute() {
        String authCookie = loginAndExtractAuthCookie(TENANT_ADMIN_EMAIL);

        Response response = LocalHttpTestClient.get(
                "http://localhost:" + port + "/api/reports/profit-vs-supplier-expenses?startDate=2026-04-01&endDate=2026-04-30",
                Map.of("Cookie", "kalles_auth_token=" + authCookie)
        );

        response.then()
                .statusCode(400)
                .body("code", equalTo("COMPANY_CONTEXT_REQUIRED"));
    }
}
