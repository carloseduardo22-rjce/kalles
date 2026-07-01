/// <reference types="cypress" />

describe("Emissao fiscal NFC-e", () => {
  const companyId = "123e4567-e89b-12d3-a456-426614174203";
  const saleId = "123e4567-e89b-12d3-a456-426614174220";
  const documentId = "123e4567-e89b-12d3-a456-426614174500";
  const productId = "123e4567-e89b-12d3-a456-426614174610";
  const fiscalOnboardingKey = "kalles:onboarding:admin-fiscal:v1:completed";

  beforeEach(() => {
    cy.intercept("GET", "**/api/auth/me", {
      statusCode: 200,
      body: {
        role: "ADMIN",
        tenantId: "123e4567-e89b-12d3-a456-426614174000",
        companyId: null,
      },
    });

    cy.intercept("GET", "**/api/companies", {
      statusCode: 200,
      body: [{ id: companyId, name: "Loja Matriz" }],
    }).as("getCompanies");

    cy.intercept("GET", "**/api/auth/csrf", {
      statusCode: 200,
      body: { token: "csrf-token" },
      headers: {
        "set-cookie": "XSRF-TOKEN=csrf-token; Path=/",
      },
    }).as("getCsrf");

    cy.setCookie("kalles_auth_token", "test-auth-token");
  });

  it("emite a NFC-e da venda finalizada na loja ativa", () => {
    cy.intercept("POST", "**/api/fiscal/nfce/issue", (req) => {
      expect(req.headers["x-company-id"]).to.eq(companyId);
      expect(req.body).to.include({
        saleId,
        model: "NFCE",
        environment: "HOMOLOGACAO",
      });

      req.reply({
        statusCode: 201,
        body: {
          id: "123e4567-e89b-12d3-a456-426614174500",
          tenantId: "123e4567-e89b-12d3-a456-426614174000",
          companyId,
          saleId,
          model: "NFCE",
          environment: "HOMOLOGACAO",
          status: "AUTORIZADO",
          accessKey: "NFCe-HOM-123e4567e89b12d3a456426614174220",
          authorizationProtocol: "HOM-135260000000001",
          rejectionReason: null,
        },
      });
    }).as("issueNfce");

    cy.visit("/admin/fiscal", {
      onBeforeLoad(win) {
        win.sessionStorage.setItem("@kalles:activeCompanyId", companyId);
        win.localStorage.setItem(fiscalOnboardingKey, "true");
      },
    });

    cy.get("[data-testid='fiscal-sale-id-input']").type(saleId);
    cy.get("[data-testid='issue-nfce-submit']").click();

    cy.wait("@issueNfce");
    cy.get("[data-testid='fiscal-document-result']")
      .scrollIntoView()
      .should("be.visible");
    cy.contains("NFC-e autorizada").scrollIntoView().should("be.visible");
    cy.contains("Chave de acesso: NFCe-HOM-123e4567e89b12d3a456426614174220")
      .scrollIntoView()
      .should("be.visible");
    cy.contains("Protocolo: HOM-135260000000001")
      .scrollIntoView()
      .should("be.visible");
  });

  it("mostra o motivo quando a NFC-e e rejeitada", () => {
    cy.intercept("POST", "**/api/fiscal/nfce/issue", {
      statusCode: 422,
      body: {
        id: "123e4567-e89b-12d3-a456-426614174501",
        tenantId: "123e4567-e89b-12d3-a456-426614174000",
        companyId,
        saleId,
        model: "NFCE",
        environment: "HOMOLOGACAO",
        status: "REJEITADO",
        accessKey: null,
        authorizationProtocol: null,
        rejectionReason: "Rejeicao: total da NFC-e difere do somatorio dos itens",
      },
    }).as("issueRejectedNfce");

    cy.visit("/admin/fiscal", {
      onBeforeLoad(win) {
        win.sessionStorage.setItem("@kalles:activeCompanyId", companyId);
        win.localStorage.setItem(fiscalOnboardingKey, "true");
      },
    });

    cy.get("[data-testid='fiscal-sale-id-input']").type(saleId);
    cy.get("[data-testid='issue-nfce-submit']").click();

    cy.wait("@issueRejectedNfce");
    cy.get("[data-testid='fiscal-document-result']")
      .scrollIntoView()
      .should("be.visible");
    cy.contains("NFC-e rejeitada").scrollIntoView().should("be.visible");
    cy.contains("Rejeicao: total da NFC-e difere do somatorio dos itens")
      .scrollIntoView()
      .should("be.visible");
  });

  it("salva dados fiscais, certificado e classificacao do produto", () => {
    cy.intercept("POST", "**/api/fiscal/preparation", (req) => {
      expect(req.headers["x-company-id"]).to.eq(companyId);
      expect(req.body).to.include({
        cnpj: "11.222.333/0001-81",
        legalName: "Kalles Comercio LTDA",
        tradeName: "Kalles Matriz",
        stateRegistration: "110.042.490.114",
        taxRegime: "SIMPLES_NACIONAL",
        cnae: "4712100",
        zipCode: "01001-000",
        stateCode: "SP",
        stateIbgeCode: 35,
        cityName: "Sao Paulo",
        cityIbgeCode: 3550308,
        district: "Se",
        street: "Praca da Se",
        number: "100",
        complement: "Loja 1",
        countryName: "Brasil",
        countryCode: 1058,
        model: "NFCE",
        environment: "HOMOLOGACAO",
        cscId: "1",
        cscToken: "CSC-HOMOLOGACAO",
        series: 1,
        nextNumber: 100,
        certificateBase64: "CERTIFICADO-A1",
        certificatePassword: "senha-segura",
      });
      expect(req.body.certificateExpiresAt).to.be.a("string");

      req.reply({
        statusCode: 201,
        body: {
          tenantId: "123e4567-e89b-12d3-a456-426614174000",
          companyId,
          ready: true,
          missingItems: [],
        },
      });
    }).as("saveFiscalPreparation");

    cy.intercept("POST", "**/api/fiscal/product-classifications", (req) => {
      expect(req.headers["x-company-id"]).to.eq(companyId);
      expect(req.body).to.deep.equal({
        productId,
        ncm: "61091000",
        cest: "2805800",
        cfop: "5102",
        cfopSale: "5102",
        origin: "0",
        csosn: "102",
        cst: null,
        unit: "UN",
        gtin: "7890000000000",
      });

      req.reply({
        statusCode: 201,
        body: {
          id: "123e4567-e89b-12d3-a456-426614174702",
          tenantId: "123e4567-e89b-12d3-a456-426614174000",
          companyId,
          productId,
          ncm: "61091000",
          cest: "2805800",
          cfop: "5102",
          cfopSale: "5102",
          origin: "0",
          csosn: "102",
          cst: null,
          unit: "UN",
          gtin: "7890000000000",
        },
      });
    }).as("saveFiscalProductClassification");

    cy.intercept("GET", "**/api/fiscal/readiness", (req) => {
      expect(req.headers["x-company-id"]).to.eq(companyId);

      req.reply({
        statusCode: 200,
        body: {
          tenantId: "123e4567-e89b-12d3-a456-426614174000",
          companyId,
          ready: true,
          missingItems: [],
        },
      });
    }).as("checkFiscalReadiness");

    cy.visit("/admin/fiscal", {
      onBeforeLoad(win) {
        win.sessionStorage.setItem("@kalles:activeCompanyId", companyId);
        win.localStorage.setItem(fiscalOnboardingKey, "true");
      },
    });

    cy.get("[data-testid='fiscal-issuer-cnpj-input']").type("11.222.333/0001-81");
    cy.get("[data-testid='fiscal-issuer-legal-name-input']").type("Kalles Comercio LTDA");
    cy.get("[data-testid='fiscal-issuer-trade-name-input']").type("Kalles Matriz");
    cy.get("[data-testid='fiscal-issuer-state-registration-input']").type("110.042.490.114");
    cy.get("[data-testid='fiscal-issuer-cnae-input']").type("4712100");

    cy.get("[data-testid='fiscal-issuer-zip-code-input']").type("01001-000");
    cy.get("[data-testid='fiscal-state-code-input']").clear().type("SP");
    cy.get("[data-testid='fiscal-issuer-state-ibge-code-input']").clear().type("35");
    cy.get("[data-testid='fiscal-issuer-city-ibge-code-input']").type("3550308");
    cy.get("[data-testid='fiscal-issuer-city-name-input']").type("Sao Paulo");
    cy.get("[data-testid='fiscal-issuer-district-input']").type("Se");
    cy.get("[data-testid='fiscal-issuer-number-input']").type("100");
    cy.get("[data-testid='fiscal-issuer-street-input']").type("Praca da Se");
    cy.get("[data-testid='fiscal-issuer-complement-input']").type("Loja 1");

    cy.get("[data-testid='fiscal-series-input']").clear().type("1");
    cy.get("[data-testid='fiscal-next-number-input']").clear().type("100");
    cy.get("[data-testid='fiscal-csc-id-input']").type("1");
    cy.get("[data-testid='fiscal-csc-token-input']").type("CSC-HOMOLOGACAO");

    cy.get("[data-testid='fiscal-certificate-content-input']")
      .scrollIntoView({ offset: { top: -160, left: 0 } })
      .type("CERTIFICADO-A1", { force: true });
    cy.get("[data-testid='fiscal-certificate-password-input']").type("senha-segura", {
      force: true,
    });
    cy.get("[data-testid='fiscal-certificate-expiration-input']").type(
      "2027-04-30T10:00",
      { force: true },
    );
    cy.get("[data-testid='save-fiscal-preparation-submit']").click();
    cy.wait("@saveFiscalPreparation");
    cy.get("[data-testid='fiscal-readiness-result']")
      .scrollIntoView()
      .should("contain", "Loja pronta para emitir.");

    cy.get("[data-testid='fiscal-product-id-input']").type(productId);
    cy.get("[data-testid='fiscal-product-ncm-input']").type("61091000");
    cy.get("[data-testid='fiscal-product-cfop-input']").clear().type("5102");
    cy.get("[data-testid='fiscal-product-cest-input']").type("2805800");
    cy.get("[data-testid='fiscal-product-cfop-sale-input']").clear().type("5102");
    cy.get("[data-testid='fiscal-product-origin-input']").clear().type("0");
    cy.get("[data-testid='fiscal-product-csosn-input']").clear().type("102");
    cy.get("[data-testid='fiscal-product-unit-input']").clear().type("UN");
    cy.get("[data-testid='fiscal-product-gtin-input']").type("7890000000000");
    cy.get("[data-testid='save-fiscal-product-classification-submit']").click();
    cy.wait("@saveFiscalProductClassification");

    cy.get("[data-testid='check-fiscal-readiness-submit']").click();
    cy.wait("@checkFiscalReadiness");
    cy.get("[data-testid='fiscal-readiness-result']").should(
      "contain",
      "Loja pronta para emitir.",
    );
  });

  it("consulta a nota, abre o DANFE e emite devolucao", () => {
    cy.intercept("GET", `**/api/fiscal/documents/${documentId}/status`, (req) => {
      expect(req.headers["x-company-id"]).to.eq(companyId);

      req.reply({
        statusCode: 200,
        body: {
          id: documentId,
          tenantId: "123e4567-e89b-12d3-a456-426614174000",
          companyId,
          saleId,
          model: "NFCE",
          environment: "HOMOLOGACAO",
          status: "AUTORIZADO",
          accessKey: "NFCe-HOM-123e4567e89b12d3a456426614174220",
          authorizationProtocol: "HOM-135260000000001",
          rejectionReason: null,
        },
      });
    }).as("queryFiscalDocumentStatus");

    cy.intercept("GET", `**/api/fiscal/documents/${documentId}/danfe`, (req) => {
      expect(req.headers["x-company-id"]).to.eq(companyId);

      req.reply({
        statusCode: 200,
        headers: { "content-type": "application/pdf" },
        body: "%PDF-1.4",
      });
    }).as("downloadDanfe");

    cy.intercept("POST", "**/api/fiscal/returns/issue", (req) => {
      expect(req.headers["x-company-id"]).to.eq(companyId);
      expect(req.body).to.deep.equal({ saleId });

      req.reply({
        statusCode: 201,
        body: {
          id: "123e4567-e89b-12d3-a456-426614174800",
          tenantId: "123e4567-e89b-12d3-a456-426614174000",
          companyId,
          saleId,
          model: "NFE_DEVOLUCAO",
          environment: "HOMOLOGACAO",
          status: "AUTORIZADO",
          accessKey: "DEV-NFCe-HOM-123e4567e89b12d3a456426614174220",
          authorizationProtocol: "DEV-HOM-135260000000001",
          rejectionReason: null,
        },
      });
    }).as("issueFiscalReturn");

    cy.visit("/admin/fiscal", {
      onBeforeLoad(win) {
        win.sessionStorage.setItem("@kalles:activeCompanyId", companyId);
        win.localStorage.setItem(fiscalOnboardingKey, "true");
        cy.stub(win, "open").as("windowOpen");
      },
    });

    cy.get("[data-testid='fiscal-document-id-input']").type(documentId);
    cy.get("[data-testid='query-fiscal-document-status-submit']").click();
    cy.wait("@queryFiscalDocumentStatus");
    cy.contains("NFC-e autorizada").scrollIntoView().should("be.visible");

    cy.get("[data-testid='download-fiscal-danfe-submit']").click();
    cy.wait("@downloadDanfe");
    cy.get("@windowOpen").should("have.been.called");

    cy.get("[data-testid='fiscal-return-sale-id-input']").type(saleId);
    cy.get("[data-testid='issue-fiscal-return-submit']").click();
    cy.wait("@issueFiscalReturn");
    cy.contains("Devolucao autorizada").scrollIntoView().should("be.visible");
  });
});
