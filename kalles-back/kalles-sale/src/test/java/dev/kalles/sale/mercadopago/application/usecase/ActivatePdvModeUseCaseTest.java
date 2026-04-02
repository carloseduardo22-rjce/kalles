package dev.kalles.sale.mercadopago.application.usecase;

import dev.kalles.sale.mercadopago.domain.Terminal;
import dev.kalles.sale.mercadopago.port.MercadoPagoTerminalPort;
import dev.kalles.sale.mercadopago.port.TerminalRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ActivatePdvModeUseCaseTest {

    @Mock
    private MercadoPagoTerminalPort terminalPort;

    @Mock
    private TerminalRepository terminalRepository;

    private ActivatePdvModeUseCase useCase;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        useCase = new ActivatePdvModeUseCase(terminalPort, terminalRepository);
    }

    @Test
    void shouldActivatePdvModeAndSaveLocally() {
        // Arrange
        UUID storeId = UUID.randomUUID();
        UUID posId = UUID.randomUUID();
        String expectedSerial = "N950NCB801293324";
        
        Terminal mockTerminal = new Terminal("NEWLAND_N950__N950NCB801293324", posId.toString(), storeId.toString(), "EXT_POS_1", "standalone");
        
        when(terminalPort.fetchTerminals(storeId, posId)).thenReturn(List.of(mockTerminal));
        when(terminalPort.changeToPdvMode("NEWLAND_N950__N950NCB801293324")).thenReturn(true);

        // Act
        useCase.execute(storeId, posId, expectedSerial);

        // Assert
        verify(terminalPort).changeToPdvMode("NEWLAND_N950__N950NCB801293324");
        
        ArgumentCaptor<Terminal> captor = ArgumentCaptor.forClass(Terminal.class);
        verify(terminalRepository).save(captor.capture());
        
        Terminal savedTerminal = captor.getValue();
        assertEquals("NEWLAND_N950__N950NCB801293324", savedTerminal.id());
        assertEquals("PDV", savedTerminal.operationMode());
    }
    
    @Test
    void shouldThrowExceptionWhenTerminalNotFound() {
        // Arrange
        UUID storeId = UUID.randomUUID();
        UUID posId = UUID.randomUUID();
        
        when(terminalPort.fetchTerminals(storeId, posId)).thenReturn(Collections.emptyList());

        // Act & Assert
        Exception ex = assertThrows(RuntimeException.class, () -> useCase.execute(storeId, posId, "ANY_SERIAL"));
        assertTrue(ex.getMessage().contains("Nenhum terminal associado a esta Loja e Caixa físico encontrado."));
    }
}
