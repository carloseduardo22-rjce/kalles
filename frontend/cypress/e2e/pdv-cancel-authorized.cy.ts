describe("Cancelamento de venda com autorizacao", () => {
  const sessionToken = "123e4567-e89b-12d3-a456-426614174301";
  const operatorId = "123e4567-e89b-12d3-a456-426614174302";
  const companyId = "123e4567-e89b-12d3-a456-426614174303";
  const supervisorId = "123e4567-e89b-12d3-a456-426614174304";

  function stubPdvContext() {
    cy.intercept("GET", "**/api/auth/me", {
      statusCode: 200,
      body: {
        role: "OPERATOR",
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

    cy.intercept("GET", "**/api/auth/csrf", {
      statusCode: 200,
      body: { token: "csrf-token" },
      headers: {
        "set-cookie": "XSRF-TOKEN=csrf-token; Path=/",
      },
    }).as("getCsrf");

    cy.intercept("GET", "**/api/cash-registers", {
      statusCode: 200,
      body: [
        {
          cashRegisterId: "123e4567-e89b-12d3-a456-426614174310",
          code: "CAIXA-01",
          description: "Caixa principal",
          active: true,
          hasActiveSession: true,
          activeSessionId: sessionToken,
          activeOperatorName: "Operador Basico",
          initialAmount: 100,
          openedAt: "2026-04-15T09:00:00Z",
          paymentIntegrationConfigured: true,
          activeSessionCashOnlyOperation: false,
        },
      ],
    }).as("getRegisters");

    cy.intercept("GET", "**/api/cash-registers/operators", {
      statusCode: 200,
      body: [
        {
          id: operatorId,
          name: "Operador Basico",
          code: "OP-BASIC",
          permissionLevel: "BASIC",
        },
      ],
    }).as("getOperators");

    cy.intercept("GET", `**/api/sales/${sessionToken}`, {
      statusCode: 404,
      body: {
        detail: "Nenhuma venda em andamento ou pendente de conclusao para esta sessao",
      },
    }).as("getCurrentSale");

    cy.intercept("POST", `**/api/sales/${sessionToken}`, {
      statusCode: 201,
      body: {
        id: "123e4567-e89b-12d3-a456-426614174320",
        sessionToken,
        state: "OPEN",
        items: [],
        payments: [],
        subtotal: 0,
        total: 0,
        amountDue: 0,
        clientId: null,
        fidelityDiscountApplied: 0,
      },
    }).as("createSale");

    cy.intercept("POST", `**/api/sales/${sessionToken}/items`, {
      statusCode: 200,
      body: {
        id: "123e4567-e89b-12d3-a456-426614174320",
        sessionToken,
        state: "OPEN",
        items: [
          {
            id: "123e4567-e89b-12d3-a456-426614174321",
            productName: "Produto PDV",
            productInternalCode: "SKU-001",
            unitPrice: 30,
            quantity: 1,
            discount: 0,
            subtotal: 30,
          },
        ],
        payments: [],
        subtotal: 30,
        total: 30,
        amountDue: 30,
        clientId: null,
        fidelityDiscountApplied: 0,
      },
    }).as("addItem");
  }

  it("solicita autorizacao quando o operador nao pode cancelar e conclui o cancelamento com supervisor", () => {
    stubPdvContext();
    cy.setCookie("kalles_auth_token", "test-auth-token");

    cy.intercept("DELETE", `**/api/sales/${sessionToken}`, (req) => {
      expect(req.headers["x-operator-id"]).to.equal(operatorId);
      expect(req.headers["x-authorizer-id"]).to.be.undefined;
      req.reply({
        statusCode: 403,
        body: {
          detail:
            "Operador nao possui permissao para cancelar vendas. Solicite autorizacao de um supervisor.",
        },
      });
    }).as("cancelSaleForbidden");

    cy.intercept(
      {
        method: "DELETE",
        url: `**/api/sales/${sessionToken}`,
        times: 1,
        headers: {
          "x-operator-id": operatorId,
          "x-authorizer-id": supervisorId,
        },
      },
      {
        statusCode: 204,
        body: "",
      },
    ).as("cancelSaleAuthorized");

    cy.visit("/pdv", {
      onBeforeLoad(win) {
        win.localStorage.setItem(
          "kalles:onboarding:pdv-terminal:v1:completed",
          "true",
        );
        win.sessionStorage.setItem(
          "kalles:active-session",
          JSON.stringify({
            sessionId: sessionToken,
            operatorId,
            cashRegisterCode: "CAIXA-01",
            operatorName: "Operador Basico",
            initialAmount: 100,
            openedAt: "2026-04-15T09:00:00Z",
            cashOnlyOperation: false,
          }),
        );
        win.sessionStorage.setItem("@kalles:activeCompanyId", companyId);
      },
    });

    cy.wait("@getCompanies");
    cy.wait("@createSale");

    cy.get("[data-testid='product-search-input']").type("SKU-001");
    cy.get("[data-testid='product-search-submit']").click();
    cy.get("[data-testid='item-quantity-dialog']").should("be.visible");
    cy.get("[data-testid='item-quantity-input']").clear().type("1");
    cy.get("[data-testid='item-quantity-confirm']").click();

    cy.wait("@addItem");
    cy.get("[data-testid='item-quantity-dialog']").should("not.exist");
    cy.get("[data-testid='cancel-sale-trigger']").click();

    cy.get("[data-testid='cancel-sale-confirm']").click();
    cy.wait("@cancelSaleForbidden");

    cy.get("[data-testid='cancel-auth-id-input']").should("be.visible");
    cy.get("[data-testid='cancel-sale-confirm']").should("be.disabled");

    cy.get("[data-testid='cancel-auth-id-input']").type(supervisorId);
    cy.get("[data-testid='cancel-sale-confirm']").click();
    cy.wait("@cancelSaleAuthorized");

    cy.get("[data-testid='sale-finished-banner']").should("be.visible");
    cy.contains("Venda cancelada.").should("be.visible");
    cy.get("[data-testid='new-sale']").should("be.visible");
  });
});
