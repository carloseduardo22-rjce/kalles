package dev.kalles.sale.mercadopago.application.usecase;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import dev.kalles.sale.mercadopago.domain.Terminal;
import dev.kalles.sale.mercadopago.port.MercadoPagoTerminalPort;
import dev.kalles.sale.mercadopago.port.TerminalRepository;

@Service
public class ActivatePdvModeUseCase {

    private final MercadoPagoTerminalPort terminalPort;
    private final TerminalRepository terminalRepository;

    public ActivatePdvModeUseCase(MercadoPagoTerminalPort terminalPort, TerminalRepository terminalRepository) {
        this.terminalPort = terminalPort;
        this.terminalRepository = terminalRepository;
    }

    public void execute(UUID storeId, UUID posId, String terminalSerial) {
        // 1. Busca os terminais integrados com a loja e caixa
        List<Terminal> terminals = terminalPort.fetchTerminals(storeId, posId);
        
        if (terminals.isEmpty()) {
            throw new RuntimeException("Nenhum terminal associado a esta Loja e Caixa físico encontrado.");
        }

        // 2. Filtra o terminal pelo serial retornado pela etiqueta traseira ("NEWLAND_N950__<SERIAL>")
        Terminal targetTerminal = terminals.stream()
                .filter(t -> t.id() != null && t.id().endsWith(terminalSerial))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Terminal com o serial fornecido não foi encontrado nesta loja/caixa."));

        // 3. Coloca o terminal vinculado neste Ponto de Venda em modo PDV
        if (!"PDV".equals(targetTerminal.operationMode())) {
            boolean success = terminalPort.changeToPdvMode(targetTerminal.id());
            if (!success) {
                throw new RuntimeException("Falha ao ativar o modo PDV no terminal.");
            }
        }
        
        // 4. Salva a configuração local para que o sistema saiba qual é o terminal ativo em pagamentos
        Terminal activeTerminal = new Terminal(
                targetTerminal.id(),
                targetTerminal.posId(),
                targetTerminal.storeId(),
                targetTerminal.externalPosId(),
                "PDV"
        );
        terminalRepository.save(activeTerminal);
    }
}
