describe("PDV em modo somente dinheiro", () => {
  function stubCashOnlySessionContext() {
    cy.intercept("GET", "**/api/auth/me", {
      statusCode: 200,
      body: {
        role: "OPERATOR",
        tenantId: "123e4567-e89b-12d3-a456-426614174000",
        companyId: "123e4567-e89b-12d3-a456-426614174001",
      },
    }).as("getMe");

    cy.intercept("GET", "**/api/companies", {
      statusCode: 200,
      body: [
        {
          id: "123e4567-e89b-12d3-a456-426614174001",
          name: "Loja Matriz",
        },
      ],
    }).as("getCompanies");

    cy.intercept("GET", "**/api/cash-registers", {
      statusCode: 200,
      body: [
        {
          cashRegisterId: "123e4567-e89b-12d3-a456-426614174030",
          code: "CAIXA-01",
          description: "Caixa principal",
          active: true,
          hasActiveSession: true,
          activeSessionId: "123e4567-e89b-12d3-a456-426614174031",
          activeOperatorName: "Operador Caixa 01",
          initialAmount: 100,
          openedAt: "2026-04-15T09:00:00Z",
          paymentIntegrationConfigured: false,
          activeSessionCashOnlyOperation: true,
        },
      ],
    }).as("getRegisters");

    cy.intercept("GET", "**/api/cash-registers/operators", {
      statusCode: 200,
      body: [
        {
          id: "123e4567-e89b-12d3-a456-426614174032",
          name: "Operador Caixa 01",
          code: "OP001",
          permissionLevel: null,
        },
      ],
    }).as("getOperators");

    cy.intercept("GET", "**/api/auth/csrf", {
      statusCode: 200,
      body: { token: "csrf-token" },
      headers: {
        "set-cookie": "XSRF-TOKEN=csrf-token; Path=/",
      },
    }).as("getCsrf");

    cy.intercept("GET", "**/api/companies", {
      statusCode: 200,
      body: [
        {
          id: "123e4567-e89b-12d3-a456-426614174001",
          name: "Loja Matriz",
        },
      ],
    }).as("getCompanies");

    cy.intercept("POST", "**/api/sales/123e4567-e89b-12d3-a456-426614174031", {
      statusCode: 201,
      body: {
        id: "123e4567-e89b-12d3-a456-426614174033",
        sessionToken: "123e4567-e89b-12d3-a456-426614174031",
        state: "OPEN",
        items: [
          {
            id: "123e4567-e89b-12d3-a456-426614174034",
            productName: "Produto teste",
            productInternalCode: "SKU-01",
            unitPrice: 25,
            quantity: 2,
            discount: 0,
            subtotal: 50,
          },
        ],
        payments: [],
        subtotal: 50,
        total: 50,
        amountDue: 50,
        clientId: null,
        fidelityDiscountApplied: 0,
      },
    }).as("createSale");
  }

  it("mostra aviso e restringe os meios de pagamento ao dinheiro", () => {
    stubCashOnlySessionContext();
    cy.setCookie("kalles_auth_token", "test-auth-token");

    cy.visit("/pdv", {
      onBeforeLoad(win) {
        win.localStorage.setItem(
          "kalles:onboarding:pdv-terminal:v1:completed",
          "true",
        );
        win.sessionStorage.setItem(
          "kalles:active-session",
          JSON.stringify({
            sessionId: "123e4567-e89b-12d3-a456-426614174031",
            operatorId: "123e4567-e89b-12d3-a456-426614174032",
            cashRegisterCode: "CAIXA-01",
            operatorName: "Operador Caixa 01",
            initialAmount: 100,
            openedAt: "2026-04-15T09:00:00Z",
            cashOnlyOperation: true,
          }),
        );
        win.sessionStorage.setItem(
          "@kalles:activeCompanyId",
          "123e4567-e89b-12d3-a456-426614174001",
        );
      },
    });

    cy.wait("@getCompanies");
    cy.wait("@createSale");
    cy.get("[data-testid='cash-only-banner']").should("be.visible");
    cy.get("[data-testid='payment-method-cash']").should("be.visible");
    cy.get("[data-testid='payment-method-pix']").should("not.exist");
    cy.get("[data-testid='payment-method-credit_card']").should("not.exist");
    cy.get("[data-testid='payment-method-debit_card']").should("not.exist");
  });
});
