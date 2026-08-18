package dev.kalles.fiscal.adapter.out.sefaz;

import br.com.swconsultoria.nfe.dom.enuns.AmbienteEnum;
import br.com.swconsultoria.nfe.dom.enuns.DocumentoEnum;
import br.com.swconsultoria.nfe.dom.enuns.EstadosEnum;
import dev.kalles.fiscal.application.port.out.SefazAuthorizationPort;
import dev.kalles.fiscal.domain.FiscalCertificate;
import dev.kalles.fiscal.domain.FiscalConfiguration;
import dev.kalles.fiscal.domain.FiscalSale;
import dev.kalles.fiscal.domain.SefazAuthorizationResult;
import dev.kalles.fiscal.exception.FiscalIntegrationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class JavaNfeSefazAuthorizationAdapter implements SefazAuthorizationPort {

    private final JavaNfeClient javaNfeClient;

    @Override
    public SefazAuthorizationResult authorize(FiscalSale sale, FiscalConfiguration configuration, FiscalCertificate certificate) {
        log.info("Enviando NFC-e a SEFAZ: venda={}, filial={}, uf={}, ambiente={}, serie={}, numero={}",
                sale.id(), sale.companyId(), configuration.stateCode(), configuration.environment(),
                configuration.series(), configuration.nextNumber());
        try {
            JavaNfeAuthorizationResponse response = javaNfeClient.authorizeNfce(new JavaNfeAuthorizationRequest(
                    toState(configuration.stateCode()),
                    toEnvironment(configuration.environment()),
                    DocumentoEnum.NFCE,
                    buildMvpXml(sale, configuration)
            ));

            if (response.authorized()) {
                log.info("NFC-e autorizada pela SEFAZ: venda={}, numero={}, chave={}, protocolo={}",
                        sale.id(), configuration.nextNumber(), response.accessKey(), response.protocol());
                return SefazAuthorizationResult.authorized(response.accessKey(), response.protocol(), response.authorizedXml());
            }

            log.warn("NFC-e rejeitada pela SEFAZ, numero {} consumido: venda={}, motivo={}",
                    configuration.nextNumber(), sale.id(), response.rejectionReason());
            return SefazAuthorizationResult.rejected(response.rejectionReason());
        } catch (Exception ex) {
            log.error("Falha de comunicacao com a SEFAZ, numero {} consumido sem retorno: venda={}",
                    configuration.nextNumber(), sale.id(), ex);
            throw new FiscalIntegrationException("Falha de comunicacao com a SEFAZ", ex);
        }
    }

    private String buildMvpXml(FiscalSale sale, FiscalConfiguration configuration) {
        return """
                <NFe>
                  <infNFe>
                    <ide><mod>65</mod><serie>%d</serie><nNF>%d</nNF><tpAmb>%s</tpAmb></ide>
                    <total>%s</total>
                  </infNFe>
                </NFe>
                """.formatted(configuration.series(), configuration.nextNumber(), configuration.environment(), sale.total());
    }

    private EstadosEnum toState(String stateCode) {
        return EstadosEnum.valueOf(stateCode);
    }

    private AmbienteEnum toEnvironment(dev.kalles.fiscal.domain.FiscalEnvironment environment) {
        return environment == dev.kalles.fiscal.domain.FiscalEnvironment.PRODUCAO
                ? AmbienteEnum.PRODUCAO
                : AmbienteEnum.HOMOLOGACAO;
    }
}
