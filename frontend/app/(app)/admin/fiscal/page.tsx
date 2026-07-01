"use client";

import { useState } from "react";
import {
  Download,
  FileCheck2,
  Loader2,
  ReceiptText,
  RotateCcw,
  Save,
  Search,
  ShieldCheck,
} from "lucide-react";
import { toast } from "sonner";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { api, ApiError } from "@/shared/services/api";

type FiscalDocumentResponse = {
  id: string;
  saleId: string;
  model: "NFCE" | "NFE_DEVOLUCAO";
  environment: "HOMOLOGACAO" | "PRODUCAO";
  status: "PENDENTE" | "AUTORIZADO" | "REJEITADO";
  accessKey?: string | null;
  authorizationProtocol?: string | null;
  rejectionReason?: string | null;
};

type FiscalConfigurationResponse = {
  stateCode: string;
  series: number;
  nextNumber: number;
};

type FiscalCertificateResponse = {
  expiresAt: string;
  active: boolean;
};

type FiscalReadinessResponse = {
  ready: boolean;
  missingItems: string[];
};

export default function FiscalPage() {
  const [saleId, setSaleId] = useState("");
  const [returnSaleId, setReturnSaleId] = useState("");
  const [documentId, setDocumentId] = useState("");
  const [productId, setProductId] = useState("");
  const [ncm, setNcm] = useState("");
  const [cfop, setCfop] = useState("5102");
  const [cfopSale, setCfopSale] = useState("5102");
  const [cest, setCest] = useState("");
  const [origin, setOrigin] = useState("0");
  const [csosn, setCsosn] = useState("102");
  const [cst, setCst] = useState("");
  const [unit, setUnit] = useState("UN");
  const [gtin, setGtin] = useState("");
  const [issuerCnpj, setIssuerCnpj] = useState("");
  const [issuerLegalName, setIssuerLegalName] = useState("");
  const [issuerTradeName, setIssuerTradeName] = useState("");
  const [issuerStateRegistration, setIssuerStateRegistration] = useState("");
  const [issuerTaxRegime, setIssuerTaxRegime] = useState("SIMPLES_NACIONAL");
  const [issuerCnae, setIssuerCnae] = useState("");
  const [issuerZipCode, setIssuerZipCode] = useState("");
  const [issuerCityName, setIssuerCityName] = useState("");
  const [issuerStateIbgeCode, setIssuerStateIbgeCode] = useState("35");
  const [issuerCityIbgeCode, setIssuerCityIbgeCode] = useState("");
  const [issuerDistrict, setIssuerDistrict] = useState("");
  const [issuerStreet, setIssuerStreet] = useState("");
  const [issuerNumber, setIssuerNumber] = useState("");
  const [issuerComplement, setIssuerComplement] = useState("");
  const [stateCode, setStateCode] = useState("SP");
  const [series, setSeries] = useState("1");
  const [nextNumber, setNextNumber] = useState("100");
  const [cscId, setCscId] = useState("");
  const [cscToken, setCscToken] = useState("");
  const [certificateBase64, setCertificateBase64] = useState("");
  const [certificatePassword, setCertificatePassword] = useState("");
  const [certificateExpiresAt, setCertificateExpiresAt] = useState("");
  const [loading, setLoading] = useState(false);
  const [savingPreparation, setSavingPreparation] = useState(false);
  const [savingConfiguration, setSavingConfiguration] = useState(false);
  const [savingCertificate, setSavingCertificate] = useState(false);
  const [savingClassification, setSavingClassification] = useState(false);
  const [savingIssuerProfile, setSavingIssuerProfile] = useState(false);
  const [savingIssuerAddress, setSavingIssuerAddress] = useState(false);
  const [checkingReadiness, setCheckingReadiness] = useState(false);
  const [checkingStatus, setCheckingStatus] = useState(false);
  const [issuingReturn, setIssuingReturn] = useState(false);
  const [document, setDocument] = useState<FiscalDocumentResponse | null>(null);
  const [configuration, setConfiguration] =
    useState<FiscalConfigurationResponse | null>(null);
  const [certificate, setCertificate] =
    useState<FiscalCertificateResponse | null>(null);
  const [readiness, setReadiness] = useState<FiscalReadinessResponse | null>(null);

  const handleSavePreparation = async () => {
    setSavingPreparation(true);

    try {
      const response = await api.post<FiscalReadinessResponse>(
        "/api/fiscal/preparation",
        {
          cnpj: issuerCnpj.trim(),
          legalName: issuerLegalName.trim(),
          tradeName: issuerTradeName.trim() || null,
          stateRegistration: issuerStateRegistration.trim(),
          taxRegime: issuerTaxRegime,
          cnae: issuerCnae.trim() || null,
          zipCode: issuerZipCode.trim(),
          stateCode: stateCode.trim().toUpperCase(),
          stateIbgeCode: Number(issuerStateIbgeCode),
          cityName: issuerCityName.trim(),
          cityIbgeCode: Number(issuerCityIbgeCode),
          district: issuerDistrict.trim(),
          street: issuerStreet.trim(),
          number: issuerNumber.trim(),
          complement: issuerComplement.trim() || null,
          countryName: "Brasil",
          countryCode: 1058,
          model: "NFCE",
          environment: "HOMOLOGACAO",
          cscId: cscId.trim() || null,
          cscToken: cscToken.trim() || null,
          series: Number(series),
          nextNumber: Number(nextNumber),
          certificateBase64: certificateBase64.trim(),
          certificatePassword,
          certificateExpiresAt: new Date(certificateExpiresAt).toISOString(),
        },
      );

      setReadiness(response);
      setCertificatePassword("");
      toast.success(
        response.ready
          ? "Loja pronta para emitir."
          : "Preparacao fiscal salva com pendencias.",
      );
    } catch (error) {
      toast.error(error instanceof Error ? error.message : "Nao foi possivel salvar.");
    } finally {
      setSavingPreparation(false);
    }
  };

  const handleSaveIssuerProfile = async () => {
    setSavingIssuerProfile(true);

    try {
      await api.post("/api/fiscal/issuer-profile", {
        cnpj: issuerCnpj.trim(),
        legalName: issuerLegalName.trim(),
        tradeName: issuerTradeName.trim() || null,
        stateRegistration: issuerStateRegistration.trim(),
        taxRegime: issuerTaxRegime,
        cnae: issuerCnae.trim() || null,
      });

      setReadiness(null);
      toast.success("Dados da empresa salvos.");
    } catch (error) {
      toast.error(error instanceof Error ? error.message : "Nao foi possivel salvar.");
    } finally {
      setSavingIssuerProfile(false);
    }
  };

  const handleSaveIssuerAddress = async () => {
    setSavingIssuerAddress(true);

    try {
      await api.post("/api/fiscal/issuer-address", {
        zipCode: issuerZipCode.trim(),
        stateCode: stateCode.trim().toUpperCase(),
        stateIbgeCode: Number(issuerStateIbgeCode),
        cityName: issuerCityName.trim(),
        cityIbgeCode: Number(issuerCityIbgeCode),
        district: issuerDistrict.trim(),
        street: issuerStreet.trim(),
        number: issuerNumber.trim(),
        complement: issuerComplement.trim() || null,
        countryName: "Brasil",
        countryCode: 1058,
      });

      setReadiness(null);
      toast.success("Endereco fiscal salvo.");
    } catch (error) {
      toast.error(error instanceof Error ? error.message : "Nao foi possivel salvar.");
    } finally {
      setSavingIssuerAddress(false);
    }
  };

  const handleCheckReadiness = async () => {
    setCheckingReadiness(true);

    try {
      const response = await api.get<FiscalReadinessResponse>("/api/fiscal/readiness");
      setReadiness(response);
    } catch (error) {
      toast.error(error instanceof Error ? error.message : "Nao foi possivel verificar.");
    } finally {
      setCheckingReadiness(false);
    }
  };

  const handleIssue = async () => {
    const trimmedSaleId = saleId.trim();

    if (!trimmedSaleId) {
      toast.error("Informe a venda para emitir a NFC-e.");
      return;
    }

    setLoading(true);
    setDocument(null);

    try {
      const response = await api.post<FiscalDocumentResponse>(
        "/api/fiscal/nfce/issue",
        {
          saleId: trimmedSaleId,
          model: "NFCE",
          environment: "HOMOLOGACAO",
        },
      );

      setDocument(response);
      toast.success("NFC-e emitida.");
    } catch (error) {
      if (error instanceof ApiError && error.status === 422) {
        const rejected = error.data as FiscalDocumentResponse;
        setDocument(rejected);
        toast.error(rejected.rejectionReason ?? "NFC-e rejeitada.");
        return;
      }

      toast.error(error instanceof Error ? error.message : "Nao foi possivel emitir a NFC-e.");
    } finally {
      setLoading(false);
    }
  };

  const handleSaveConfiguration = async () => {
    setSavingConfiguration(true);

    try {
      const response = await api.post<FiscalConfigurationResponse>(
        "/api/fiscal/configurations",
        {
          model: "NFCE",
          environment: "HOMOLOGACAO",
          stateCode: stateCode.trim().toUpperCase(),
          cscId: cscId.trim() || null,
          cscToken: cscToken.trim() || null,
          series: Number(series),
          nextNumber: Number(nextNumber),
        },
      );

      setConfiguration(response);
      toast.success("Configuracao fiscal salva.");
    } catch (error) {
      toast.error(error instanceof Error ? error.message : "Nao foi possivel salvar.");
    } finally {
      setSavingConfiguration(false);
    }
  };

  const handleRegisterCertificate = async () => {
    setSavingCertificate(true);

    try {
      const response = await api.post<FiscalCertificateResponse>(
        "/api/fiscal/certificates",
        {
          certificateBase64: certificateBase64.trim(),
          password: certificatePassword,
          expiresAt: new Date(certificateExpiresAt).toISOString(),
        },
      );

      setCertificate(response);
      setCertificatePassword("");
      setReadiness(null);
      toast.success("Certificado fiscal salvo.");
    } catch (error) {
      toast.error(error instanceof Error ? error.message : "Nao foi possivel salvar.");
    } finally {
      setSavingCertificate(false);
    }
  };

  const handleSaveClassification = async () => {
    setSavingClassification(true);

    try {
      await api.post("/api/fiscal/product-classifications", {
        productId: productId.trim(),
        ncm: ncm.trim(),
        cest: cest.trim() || null,
        cfop: cfop.trim() || null,
        cfopSale: cfopSale.trim() || cfop.trim() || null,
        origin: origin.trim() || null,
        csosn: csosn.trim() || null,
        cst: cst.trim() || null,
        unit: unit.trim() || null,
        gtin: gtin.trim() || null,
      });

      toast.success("Classificacao fiscal salva.");
    } catch (error) {
      toast.error(error instanceof Error ? error.message : "Nao foi possivel salvar.");
    } finally {
      setSavingClassification(false);
    }
  };

  const handleStatus = async () => {
    const trimmedDocumentId = documentId.trim();

    if (!trimmedDocumentId) {
      toast.error("Informe a nota fiscal.");
      return;
    }

    setCheckingStatus(true);

    try {
      const response = await api.get<FiscalDocumentResponse>(
        `/api/fiscal/documents/${trimmedDocumentId}/status`,
      );
      setDocument(response);
    } catch (error) {
      toast.error(error instanceof Error ? error.message : "Nao foi possivel consultar.");
    } finally {
      setCheckingStatus(false);
    }
  };

  const handleDownloadDanfe = async () => {
    const trimmedDocumentId = documentId.trim() || document?.id;

    if (!trimmedDocumentId) {
      toast.error("Informe a nota fiscal.");
      return;
    }

    try {
      const blob = await api.download(
        `/api/fiscal/documents/${trimmedDocumentId}/danfe`,
      );
      const url = URL.createObjectURL(blob);
      window.open(url, "_blank", "noopener,noreferrer");
      window.setTimeout(() => URL.revokeObjectURL(url), 30_000);
    } catch (error) {
      toast.error(error instanceof Error ? error.message : "DANFE indisponivel.");
    }
  };

  const handleIssueReturn = async () => {
    const trimmedSaleId = returnSaleId.trim();

    if (!trimmedSaleId) {
      toast.error("Informe a venda devolvida.");
      return;
    }

    setIssuingReturn(true);

    try {
      const response = await api.post<FiscalDocumentResponse>(
        "/api/fiscal/returns/issue",
        { saleId: trimmedSaleId },
      );

      setDocument(response);
      toast.success("Nota de devolucao emitida.");
    } catch (error) {
      toast.error(error instanceof Error ? error.message : "Nao foi possivel emitir.");
    } finally {
      setIssuingReturn(false);
    }
  };

  return (
    <main
      className="min-h-screen bg-slate-50 px-4 py-6 md:px-8"
      data-onboarding="fiscal-page"
    >
      <div className="mx-auto flex max-w-6xl flex-col gap-6">
        <header
          className="flex flex-col gap-3 border-b border-slate-200 pb-5"
          data-onboarding="fiscal-header"
        >
          <div className="flex items-center gap-3">
            <div className="flex h-10 w-10 items-center justify-center rounded-md bg-emerald-100 text-emerald-700">
              <ReceiptText className="h-5 w-5" />
            </div>
            <div>
              <h1 className="text-2xl font-semibold text-slate-950">
                Emissao fiscal
              </h1>
              <p className="text-sm text-slate-600">
                Prepare a loja, acompanhe notas e emita documentos fiscais.
              </p>
            </div>
          </div>
        </header>

        <section
          className="grid gap-4 rounded-md border border-slate-200 bg-white p-5"
          data-onboarding="fiscal-issuer"
        >
          <div className="flex items-center gap-2 text-slate-950">
            <ShieldCheck className="h-5 w-5 text-emerald-700" />
            <h2 className="text-base font-semibold">Empresa emissora</h2>
          </div>

          <div className="grid gap-3 md:grid-cols-3">
            <label className="grid gap-2 text-sm font-medium text-slate-700">
              CNPJ
              <Input
                value={issuerCnpj}
                onChange={(event) => setIssuerCnpj(event.target.value)}
                data-testid="fiscal-issuer-cnpj-input"
              />
            </label>
            <label className="grid gap-2 text-sm font-medium text-slate-700 md:col-span-2">
              Razao social
              <Input
                value={issuerLegalName}
                onChange={(event) => setIssuerLegalName(event.target.value)}
                data-testid="fiscal-issuer-legal-name-input"
              />
            </label>
          </div>

          <div className="grid gap-3 md:grid-cols-4">
            <label className="grid gap-2 text-sm font-medium text-slate-700">
              Nome fantasia
              <Input
                value={issuerTradeName}
                onChange={(event) => setIssuerTradeName(event.target.value)}
                data-testid="fiscal-issuer-trade-name-input"
              />
            </label>
            <label className="grid gap-2 text-sm font-medium text-slate-700">
              Inscricao estadual
              <Input
                value={issuerStateRegistration}
                onChange={(event) => setIssuerStateRegistration(event.target.value)}
                data-testid="fiscal-issuer-state-registration-input"
              />
            </label>
            <label className="grid gap-2 text-sm font-medium text-slate-700">
              Regime
              <select
                value={issuerTaxRegime}
                onChange={(event) => setIssuerTaxRegime(event.target.value)}
                className="h-10 rounded-md border border-slate-300 bg-white px-3 text-sm text-slate-900"
                data-testid="fiscal-issuer-tax-regime-select"
              >
                <option value="SIMPLES_NACIONAL">Simples Nacional</option>
                <option value="LUCRO_PRESUMIDO">Lucro Presumido</option>
                <option value="LUCRO_REAL">Lucro Real</option>
              </select>
            </label>
            <label className="grid gap-2 text-sm font-medium text-slate-700">
              CNAE
              <Input
                value={issuerCnae}
                onChange={(event) => setIssuerCnae(event.target.value)}
                data-testid="fiscal-issuer-cnae-input"
              />
            </label>
          </div>
        </section>

        <section
          className="grid gap-4 rounded-md border border-slate-200 bg-white p-5"
          data-onboarding="fiscal-address"
        >
          <div className="flex items-center gap-2 text-slate-950">
            <ShieldCheck className="h-5 w-5 text-emerald-700" />
            <h2 className="text-base font-semibold">Endereco fiscal</h2>
          </div>

          <div className="grid gap-3 md:grid-cols-4">
            <label className="grid gap-2 text-sm font-medium text-slate-700">
              CEP
              <Input
                value={issuerZipCode}
                onChange={(event) => setIssuerZipCode(event.target.value)}
                data-testid="fiscal-issuer-zip-code-input"
              />
            </label>
            <label className="grid gap-2 text-sm font-medium text-slate-700">
              UF
              <Input
                value={stateCode}
                onChange={(event) => setStateCode(event.target.value)}
                maxLength={2}
                data-testid="fiscal-state-code-input"
              />
            </label>
            <label className="grid gap-2 text-sm font-medium text-slate-700">
              Codigo UF
              <Input
                type="number"
                value={issuerStateIbgeCode}
                onChange={(event) => setIssuerStateIbgeCode(event.target.value)}
                data-testid="fiscal-issuer-state-ibge-code-input"
              />
            </label>
            <label className="grid gap-2 text-sm font-medium text-slate-700">
              Codigo cidade
              <Input
                type="number"
                value={issuerCityIbgeCode}
                onChange={(event) => setIssuerCityIbgeCode(event.target.value)}
                data-testid="fiscal-issuer-city-ibge-code-input"
              />
            </label>
          </div>

          <div className="grid gap-3 md:grid-cols-3">
            <label className="grid gap-2 text-sm font-medium text-slate-700">
              Cidade
              <Input
                value={issuerCityName}
                onChange={(event) => setIssuerCityName(event.target.value)}
                data-testid="fiscal-issuer-city-name-input"
              />
            </label>
            <label className="grid gap-2 text-sm font-medium text-slate-700">
              Bairro
              <Input
                value={issuerDistrict}
                onChange={(event) => setIssuerDistrict(event.target.value)}
                data-testid="fiscal-issuer-district-input"
              />
            </label>
            <label className="grid gap-2 text-sm font-medium text-slate-700">
              Numero
              <Input
                value={issuerNumber}
                onChange={(event) => setIssuerNumber(event.target.value)}
                data-testid="fiscal-issuer-number-input"
              />
            </label>
          </div>

          <div className="grid gap-3 md:grid-cols-[2fr_1fr] md:items-end">
            <label className="grid gap-2 text-sm font-medium text-slate-700">
              Logradouro
              <Input
                value={issuerStreet}
                onChange={(event) => setIssuerStreet(event.target.value)}
                data-testid="fiscal-issuer-street-input"
              />
            </label>
            <label className="grid gap-2 text-sm font-medium text-slate-700">
              Complemento
              <Input
                value={issuerComplement}
                onChange={(event) => setIssuerComplement(event.target.value)}
                data-testid="fiscal-issuer-complement-input"
              />
            </label>
          </div>
        </section>

        <div className="grid gap-5 lg:grid-cols-2">
          <section
            className="grid gap-4 rounded-md border border-slate-200 bg-white p-5"
            data-onboarding="fiscal-credentials"
          >
            <div className="flex items-center gap-2 text-slate-950">
              <ShieldCheck className="h-5 w-5 text-emerald-700" />
              <h2 className="text-base font-semibold">Numeracao e CSC</h2>
            </div>

            <div className="grid gap-3 sm:grid-cols-2">
              <label className="grid gap-2 text-sm font-medium text-slate-700">
                Serie
                <Input
                  type="number"
                  min="1"
                  value={series}
                  onChange={(event) => setSeries(event.target.value)}
                  data-testid="fiscal-series-input"
                />
              </label>
              <label className="grid gap-2 text-sm font-medium text-slate-700">
                Proxima nota
                <Input
                  type="number"
                  min="1"
                  value={nextNumber}
                  onChange={(event) => setNextNumber(event.target.value)}
                  data-testid="fiscal-next-number-input"
                />
              </label>
            </div>

            <div className="grid gap-3 sm:grid-cols-2">
              <label className="grid gap-2 text-sm font-medium text-slate-700">
                Codigo CSC
                <Input
                  value={cscId}
                  onChange={(event) => setCscId(event.target.value)}
                  data-testid="fiscal-csc-id-input"
                />
              </label>
              <label className="grid gap-2 text-sm font-medium text-slate-700">
                Token CSC
                <Input
                  value={cscToken}
                  onChange={(event) => setCscToken(event.target.value)}
                  data-testid="fiscal-csc-token-input"
                />
              </label>
            </div>

          </section>

          <section
            className="grid gap-4 rounded-md border border-slate-200 bg-white p-5"
            data-onboarding="fiscal-certificate"
          >
            <div className="flex items-center gap-2 text-slate-950">
              <ShieldCheck className="h-5 w-5 text-emerald-700" />
              <h2 className="text-base font-semibold">Certificado A1</h2>
            </div>

            <label className="grid gap-2 text-sm font-medium text-slate-700">
              Conteudo do certificado
              <Input
                value={certificateBase64}
                onChange={(event) => setCertificateBase64(event.target.value)}
                placeholder="Cole o conteudo do certificado"
                data-testid="fiscal-certificate-content-input"
              />
            </label>

            <div className="grid gap-3 sm:grid-cols-2">
              <label className="grid gap-2 text-sm font-medium text-slate-700">
                Senha
                <Input
                  type="password"
                  value={certificatePassword}
                  onChange={(event) => setCertificatePassword(event.target.value)}
                  data-testid="fiscal-certificate-password-input"
                />
              </label>
              <label className="grid gap-2 text-sm font-medium text-slate-700">
                Valido ate
                <Input
                  type="datetime-local"
                  value={certificateExpiresAt}
                  onChange={(event) => setCertificateExpiresAt(event.target.value)}
                  data-testid="fiscal-certificate-expiration-input"
                />
              </label>
            </div>

            {certificate ? (
              <p className="text-sm text-emerald-700">
                Certificado ativo ate {new Date(certificate.expiresAt).toLocaleDateString("pt-BR")}.
              </p>
            ) : null}
          </section>
        </div>

        <section
          className="grid gap-3 rounded-md border border-slate-200 bg-white p-5"
          data-onboarding="fiscal-preparation-action"
        >
          <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
            <div className="grid gap-1">
              <h2 className="text-base font-semibold text-slate-950">
                Preparacao fiscal da loja
              </h2>
              <p className="text-sm text-slate-600">
                Salve os dados acima juntos e confira se a loja ficou pronta para emitir.
              </p>
            </div>
            <div className="flex flex-wrap gap-3">
              <Button
                type="button"
                variant="outline"
                onClick={handleCheckReadiness}
                disabled={checkingReadiness}
                data-testid="check-fiscal-readiness-submit"
              >
                {checkingReadiness ? (
                  <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                ) : (
                  <ShieldCheck className="mr-2 h-4 w-4" />
                )}
                Verificar loja
              </Button>
              <Button
                type="button"
                onClick={handleSavePreparation}
                disabled={savingPreparation}
                data-testid="save-fiscal-preparation-submit"
              >
                {savingPreparation ? (
                  <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                ) : (
                  <Save className="mr-2 h-4 w-4" />
                )}
                Salvar preparacao fiscal
              </Button>
            </div>
          </div>

          {readiness ? (
            <div
              className={
                readiness.ready
                  ? "rounded-md border border-emerald-200 bg-emerald-50 p-3 text-sm text-emerald-800"
                  : "rounded-md border border-amber-200 bg-amber-50 p-3 text-sm text-amber-800"
              }
              data-testid="fiscal-readiness-result"
            >
              {readiness.ready ? (
                <p>Loja pronta para emitir.</p>
              ) : (
                <div className="grid gap-2">
                  <p>Faltam dados para emitir.</p>
                  <ul className="list-inside list-disc">
                    {readiness.missingItems.map((item) => (
                      <li key={item}>{item}</li>
                    ))}
                  </ul>
                </div>
              )}
            </div>
          ) : null}
        </section>

        <section
          className="grid gap-4 rounded-md border border-slate-200 bg-white p-5"
          data-onboarding="fiscal-product"
        >
          <div className="flex items-center gap-2 text-slate-950">
            <ReceiptText className="h-5 w-5 text-emerald-700" />
            <h2 className="text-base font-semibold">Classificacao de produto</h2>
          </div>

          <div className="grid gap-3 md:grid-cols-4">
            <label className="grid gap-2 text-sm font-medium text-slate-700 md:col-span-2">
              Produto
              <Input
                value={productId}
                onChange={(event) => setProductId(event.target.value)}
                placeholder="Codigo do produto"
                data-testid="fiscal-product-id-input"
              />
            </label>
            <label className="grid gap-2 text-sm font-medium text-slate-700">
              NCM
              <Input
                value={ncm}
                onChange={(event) => setNcm(event.target.value)}
                data-testid="fiscal-product-ncm-input"
              />
            </label>
            <label className="grid gap-2 text-sm font-medium text-slate-700">
              CFOP
              <Input
                value={cfop}
                onChange={(event) => setCfop(event.target.value)}
                data-testid="fiscal-product-cfop-input"
              />
            </label>
          </div>

          <div className="grid gap-3 md:grid-cols-6">
            <label className="grid gap-2 text-sm font-medium text-slate-700">
              CEST
              <Input
                value={cest}
                onChange={(event) => setCest(event.target.value)}
                data-testid="fiscal-product-cest-input"
              />
            </label>
            <label className="grid gap-2 text-sm font-medium text-slate-700">
              CFOP venda
              <Input
                value={cfopSale}
                onChange={(event) => setCfopSale(event.target.value)}
                data-testid="fiscal-product-cfop-sale-input"
              />
            </label>
            <label className="grid gap-2 text-sm font-medium text-slate-700">
              Origem
              <Input
                value={origin}
                onChange={(event) => setOrigin(event.target.value)}
                data-testid="fiscal-product-origin-input"
              />
            </label>
            <label className="grid gap-2 text-sm font-medium text-slate-700">
              CSOSN
              <Input
                value={csosn}
                onChange={(event) => setCsosn(event.target.value)}
                data-testid="fiscal-product-csosn-input"
              />
            </label>
            <label className="grid gap-2 text-sm font-medium text-slate-700">
              CST
              <Input
                value={cst}
                onChange={(event) => setCst(event.target.value)}
                data-testid="fiscal-product-cst-input"
              />
            </label>
            <label className="grid gap-2 text-sm font-medium text-slate-700">
              Unidade
              <Input
                value={unit}
                onChange={(event) => setUnit(event.target.value)}
                data-testid="fiscal-product-unit-input"
              />
            </label>
          </div>

          <div className="grid gap-3 sm:grid-cols-[1fr_auto] sm:items-end">
            <label className="grid gap-2 text-sm font-medium text-slate-700">
              GTIN
              <Input
                value={gtin}
                onChange={(event) => setGtin(event.target.value)}
                data-testid="fiscal-product-gtin-input"
              />
            </label>
            <Button
              type="button"
              onClick={handleSaveClassification}
              disabled={savingClassification}
              data-testid="save-fiscal-product-classification-submit"
            >
              {savingClassification ? (
                <Loader2 className="mr-2 h-4 w-4 animate-spin" />
              ) : (
                <Save className="mr-2 h-4 w-4" />
              )}
              Salvar produto
            </Button>
          </div>
        </section>

        <div className="grid gap-5 lg:grid-cols-2" data-onboarding="fiscal-operations">
          <section className="grid gap-4 rounded-md border border-slate-200 bg-white p-5">
            <div className="flex items-center gap-2 text-slate-950">
              <FileCheck2 className="h-5 w-5 text-emerald-700" />
              <h2 className="text-base font-semibold">NFC-e da venda</h2>
            </div>

            <label className="grid gap-2 text-sm font-medium text-slate-700">
              Venda
              <Input
                value={saleId}
                onChange={(event) => setSaleId(event.target.value)}
                placeholder="Codigo da venda"
                data-testid="fiscal-sale-id-input"
              />
            </label>

            <Button
              type="button"
              onClick={handleIssue}
              disabled={loading}
              data-testid="issue-nfce-submit"
            >
              {loading ? (
                <Loader2 className="mr-2 h-4 w-4 animate-spin" />
              ) : (
                <FileCheck2 className="mr-2 h-4 w-4" />
              )}
              Emitir NFC-e
            </Button>
          </section>

          <section className="grid gap-4 rounded-md border border-slate-200 bg-white p-5">
            <div className="flex items-center gap-2 text-slate-950">
              <RotateCcw className="h-5 w-5 text-emerald-700" />
              <h2 className="text-base font-semibold">Nota de devolucao</h2>
            </div>

            <label className="grid gap-2 text-sm font-medium text-slate-700">
              Venda devolvida
              <Input
                value={returnSaleId}
                onChange={(event) => setReturnSaleId(event.target.value)}
                placeholder="Codigo da venda"
                data-testid="fiscal-return-sale-id-input"
              />
            </label>

            <Button
              type="button"
              onClick={handleIssueReturn}
              disabled={issuingReturn}
              data-testid="issue-fiscal-return-submit"
            >
              {issuingReturn ? (
                <Loader2 className="mr-2 h-4 w-4 animate-spin" />
              ) : (
                <RotateCcw className="mr-2 h-4 w-4" />
              )}
              Emitir devolucao
            </Button>
          </section>
        </div>

        <section
          className="grid gap-4 rounded-md border border-slate-200 bg-white p-5"
          data-onboarding="fiscal-documents"
        >
          <div className="flex items-center gap-2 text-slate-950">
            <Search className="h-5 w-5 text-emerald-700" />
            <h2 className="text-base font-semibold">Acompanhar nota</h2>
          </div>

          <div className="grid gap-3 sm:grid-cols-[1fr_auto_auto] sm:items-end">
            <label className="grid gap-2 text-sm font-medium text-slate-700">
              Nota fiscal
              <Input
                value={documentId}
                onChange={(event) => setDocumentId(event.target.value)}
                placeholder="Codigo da nota"
                data-testid="fiscal-document-id-input"
              />
            </label>
            <Button
              type="button"
              variant="outline"
              onClick={handleStatus}
              disabled={checkingStatus}
              data-testid="query-fiscal-document-status-submit"
            >
              {checkingStatus ? (
                <Loader2 className="mr-2 h-4 w-4 animate-spin" />
              ) : (
                <Search className="mr-2 h-4 w-4" />
              )}
              Consultar
            </Button>
            <Button
              type="button"
              variant="outline"
              onClick={handleDownloadDanfe}
              data-testid="download-fiscal-danfe-submit"
            >
              <Download className="mr-2 h-4 w-4" />
              Abrir DANFE
            </Button>
          </div>
        </section>

        {document ? (
          <section
            className="grid gap-3 rounded-md border border-slate-200 bg-white p-5"
            data-testid="fiscal-document-result"
          >
            <div className="flex items-center justify-between gap-3">
              <h2 className="text-lg font-semibold text-slate-950">
                {document.status === "AUTORIZADO"
                  ? document.model === "NFE_DEVOLUCAO"
                    ? "Devolucao autorizada"
                    : "NFC-e autorizada"
                  : document.model === "NFE_DEVOLUCAO"
                    ? "Devolucao rejeitada"
                    : "NFC-e rejeitada"}
              </h2>
              <span className="rounded-md bg-slate-100 px-2 py-1 text-xs font-medium text-slate-700">
                {document.status}
              </span>
            </div>

            {document.accessKey ? (
              <p className="break-all text-sm text-slate-600">
                Chave de acesso: {document.accessKey}
              </p>
            ) : null}

            {document.authorizationProtocol ? (
              <p className="text-sm text-slate-600">
                Protocolo: {document.authorizationProtocol}
              </p>
            ) : null}

            {document.rejectionReason ? (
              <p className="text-sm text-red-700">
                {document.rejectionReason}
              </p>
            ) : null}
          </section>
        ) : null}
      </div>
    </main>
  );
}
