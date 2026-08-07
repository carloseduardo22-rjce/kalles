package dev.kalles.fiscal.adapter.out.sefaz;

import br.com.swconsultoria.nfe.dom.enuns.AmbienteEnum;
import br.com.swconsultoria.nfe.dom.enuns.DocumentoEnum;
import br.com.swconsultoria.nfe.dom.enuns.EstadosEnum;

public record JavaNfeAuthorizationRequest(
        EstadosEnum state,
        AmbienteEnum environment,
        DocumentoEnum document,
        String xml
) {
}
