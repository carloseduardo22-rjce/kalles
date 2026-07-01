package dev.kalles.sale.fiscal.adapter.out.sefaz;

import br.com.swconsultoria.nfe.dom.enuns.AmbienteEnum;
import br.com.swconsultoria.nfe.dom.enuns.DocumentoEnum;
import br.com.swconsultoria.nfe.dom.enuns.EstadosEnum;
import dev.kalles.sale.fiscal.application.port.out.SefazAuthorizationPort;
import dev.kalles.sale.fiscal.domain.FiscalCertificate;
import dev.kalles.sale.fiscal.domain.FiscalConfiguration;
import dev.kalles.sale.fiscal.domain.FiscalSale;
import dev.kalles.sale.fiscal.domain.SefazAuthorizationResult;
import dev.kalles.sale.fiscal.exception.FiscalIntegrationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JavaNfeSefazAuthorizationAdapter implements SefazAuthorizationPort {

    private final JavaNfeClient javaNfeClient;

    @Override
    public SefazAuthorizationResult authorize(FiscalSale sale, FiscalConfiguration configuration, FiscalCertificate certificate) {
        try {
            JavaNfeAuthorizationResponse response = javaNfeClient.authorizeNfce(new JavaNfeAuthorizationRequest(
                    toState(configuration.stateCode()),
                    toEnvironment(configuration.environment()),
                    DocumentoEnum.NFCE,
                    buildMvpXml(sale, configuration)
            ));

            if (response.authorized()) {
                return SefazAuthorizationResult.authorized(response.accessKey(), response.protocol(), response.authorizedXml());
            }
            return SefazAuthorizationResult.rejected(response.rejectionReason());
        } catch (Exception ex) {
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

    private AmbienteEnum toEnvironment(dev.kalles.sale.fiscal.domain.FiscalEnvironment environment) {
        return environment == dev.kalles.sale.fiscal.domain.FiscalEnvironment.PRODUCAO
                ? AmbienteEnum.PRODUCAO
                : AmbienteEnum.HOMOLOGACAO;
    }
}
