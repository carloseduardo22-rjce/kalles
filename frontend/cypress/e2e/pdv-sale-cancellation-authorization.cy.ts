describe("Cancelamento de venda com autorizacao", () => {
  const sessionToken = "123e4567-e89b-12d3-a456-426614174301";
  const operatorId = "123e4567-e89b-12d3-a456-426614174302";
  const companyId = "123e4567-e89b-12d3-a456-426614174303";
  const authorizerId = "123e4567-e89b-12d3-a456-426614174304";

  function stubCancellationFlow() {
    cy.intercept("GET", "**/api/auth/me", {
      statusCode: 200,
      body: {
        role: "OPERATOR",
        tenantId: "123e4567-e89b-12d3-a456-426614174000",
        companyId,
      },
    }).as("getMe");

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
          activeOperatorName: "Operador Caixa 01",
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
          name: "Operador Caixa 01",
          code: "OP001",
          permissionLevel: "BASIC",
        },
      ],
    }).as("getOperators");

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
        amountDue: 0,
        clientId: null,
        fidelityDiscountApplied: 0,
      },
    }).as("addItem");

    cy.intercept(
      {
        method: "DELETE",
        url: `**/api/sales/${sessionToken}`,
        times: 1,
      },
      {
        statusCode: 403,
        body: {
          title: "Operacao nao permitida",
          detail:
            "Operador nao possui permissao para cancelar vendas. Solicite autorizacao de um supervisor.",
          status: 403,
        },
      },
    ).as("cancelForbidden");

    cy.intercept("DELETE", `**/api/sales/${sessionToken}`, {
      statusCode: 204,
      body: "",
    }).as("cancelAuthorized");
  }

  it("solicita autorizador quando o operador nao pode cancelar sozinho", () => {
    stubCancellationFlow();

    cy.visit("/pdv", {
      onBeforeLoad(win) {
        win.sessionStorage.setItem(
          "kalles:active-session",
          JSON.stringify({
            sessionId: sessionToken,
            operatorId,
            cashRegisterCode: "CAIXA-01",
            operatorName: "Operador Caixa 01",
            initialAmount: 100,
            openedAt: "2026-04-15T09:00:00Z",
            cashOnlyOperation: false,
          }),
        );
        win.sessionStorage.setItem("@kalles:activeCompanyId", companyId);
      },
    });

    cy.wait("@createSale");
    cy.get("[data-testid='product-search-input']").type("SKU-001");
    cy.get("[data-testid='product-search-submit']").click();
    cy.wait("@addItem");

    cy.get("[data-testid='cancel-sale-trigger']").click();
    cy.get("[data-testid='cancel-sale-confirm']").click();
    cy.wait("@cancelForbidden")
      .its("request.headers")
      .should("have.property", "x-operator-id", operatorId);

    cy.contains("Autorizacao de supervisor necessaria").should("be.visible");
    cy.get("[data-testid='cancel-auth-id-input']").type(authorizerId);
    cy.get("[data-testid='cancel-sale-confirm']").click();

    cy.wait("@cancelAuthorized")
      .its("request.headers")
      .should("have.property", "x-authorizer-id", authorizerId);

    cy.get("[data-testid='sale-finished-banner']").should("be.visible");
  });
});
