describe("Historico de vendas", () => {
  const companyId = "123e4567-e89b-12d3-a456-426614174203";

  function stubSession() {
    cy.intercept("GET", "**/api/auth/me", {
      statusCode: 200,
      body: {
        role: "ADMIN",
        tenantId: "123e4567-e89b-12d3-a456-426614174000",
        companyId,
      },
    }).as("getMe");

    cy.intercept("GET", "**/api/companies", {
      statusCode: 200,
      body: [
        {
          id: companyId,
          name: "Loja Matriz",
        },
      ],
    }).as("getCompanies");

    cy.intercept("GET", "**/api/cash-registers", {
      statusCode: 200,
      body: [],
    }).as("getRegisters");
  }

  it("lista vendas por periodo e exporta para Excel", () => {
    stubSession();

    cy.intercept("GET", "**/api/sales/history?*", {
      statusCode: 200,
      body: [
        {
          id: "123e4567-e89b-12d3-a456-426614174220",
          version: 0,
          sessionToken: "123e4567-e89b-12d3-a456-426614174201",
          companyId,
          state: "COMPLETED",
          clientId: null,
          subtotal: 120,
          total: 115,
          amountDue: 0,
          fidelityDiscountApplied: 5,
          pointsEarned: 12,
          openedAt: "2026-04-20T10:00:00",
          items: [
            {
              id: "123e4567-e89b-12d3-a456-426614174221",
              saleId: "123e4567-e89b-12d3-a456-426614174220",
              productId: "123e4567-e89b-12d3-a456-426614174222",
              productName: "Camiseta Basica",
              productInternalCode: "SKU-001",
              unitPrice: 120,
              quantity: 1,
              discount: 0,
              subtotal: 120,
            },
          ],
          payments: [
            {
              id: "123e4567-e89b-12d3-a456-426614174223",
              saleId: "123e4567-e89b-12d3-a456-426614174220",
              method: "CASH",
              amount: 120,
              changeAmount: 5,
              confirmed: true,
              transactionId: "cash-1",
              createdAt: "2026-04-20T10:05:00",
              updatedAt: "2026-04-20T10:05:00",
            },
          ],
        },
      ],
    }).as("getSalesHistory");

    cy.intercept("GET", "**/api/sales/history/export?*", {
      statusCode: 200,
      headers: {
        "content-type":
          "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
      },
      body: "PK\u0003\u0004fake-xlsx",
    }).as("exportSalesHistory");

    cy.setCookie("kalles_auth_token", "test-auth-token");

    cy.visit("/vendas", {
      onBeforeLoad(win) {
        win.sessionStorage.setItem("@kalles:activeCompanyId", companyId);
      },
    });

    cy.wait("@getSalesHistory");
    cy.get("[data-testid='sales-history-page']").should("be.visible");
    cy.get("[data-testid='sales-history-row']").should("have.length", 1);
    cy.contains("Camiseta Basica").should("be.visible");
    cy.contains("R$ 115,00").should("be.visible");

    cy.get("[data-testid='sales-history-state']").select("COMPLETED");
    cy.wait("@getSalesHistory");

    cy.get("[data-testid='sales-history-export']").click();
    cy.wait("@exportSalesHistory");
  });
});
