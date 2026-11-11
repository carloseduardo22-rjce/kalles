package dev.kalles.cashregister.service;

import dev.kalles.cashregister.dto.CreateCashRegisterRequest;
import dev.kalles.cashregister.entity.CashRegister;
import dev.kalles.cashregister.exception.CashRegisterCodeAlreadyExistsException;
import dev.kalles.cashregister.repository.CashRegisterRepository;
import dev.kalles.security.context.RequestContext;
import dev.kalles.security.exception.CompanyContextRequiredException;
import dev.kalles.testsupport.RequestContextExtension;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("CashRegisterCommandService - Cadastro de caixas")
class CashRegisterCommandServiceTest {

    private static final UUID COMPANY_ID = UUID.fromString("7ec7d531-95a4-4f19-b989-459e2c1ea701");

    @RegisterExtension
    static final RequestContextExtension REQUEST_CONTEXT = RequestContextExtension.company(COMPANY_ID);

    @Mock
    private CashRegisterRepository repository;

    @InjectMocks
    private CashRegisterCommandService service;

    @Test
    @DisplayName("Primeiro caixa da loja recebe CAIXA-01")
    void shouldGenerateFirstCodeForStoreWithoutRegisters() {
        stubExistingCodes();
        stubSave();

        CashRegister created = service.create(new CreateCashRegisterRequest("Caixa da entrada", null));

        assertEquals("CAIXA-01", created.getCode());
        assertEquals("Caixa da entrada", created.getDescription());
        assertEquals(COMPANY_ID, created.getCompanyId());
    }

    @Test
    @DisplayName("Proximo codigo vem depois do maior numero, sem reaproveitar lacunas")
    void shouldFollowHighestNumberWithoutReusingGaps() {
        stubExistingCodes("CAIXA-01", "CAIXA-03");
        stubSave();

        CashRegister created = service.create(new CreateCashRegisterRequest("Caixa novo", null));

        assertEquals("CAIXA-04", created.getCode());
    }

    @Test
    @DisplayName("Codigos fora do padrao nao entram na sequencia")
    void shouldIgnoreCodesOutsideTheSequentialPattern() {
        stubExistingCodes("CAIXA-PRINCIPAL", "CAIXA-02-ANTIGO");
        stubSave();

        CashRegister created = service.create(new CreateCashRegisterRequest("Caixa novo", null));

        assertEquals("CAIXA-01", created.getCode());
    }

    @Test
    @DisplayName("Sequencia passa de dois digitos depois do CAIXA-99")
    void shouldGrowPastTwoDigits() {
        stubExistingCodes("CAIXA-99");
        stubSave();

        CashRegister created = service.create(new CreateCashRegisterRequest("Caixa novo", null));

        assertEquals("CAIXA-100", created.getCode());
    }

    @Test
    @DisplayName("Cadastro simultaneo com o mesmo codigo responde conflito")
    void shouldReportConflictWhenAnotherRegisterTookTheCode() {
        stubExistingCodes("CAIXA-01");
        when(repository.saveAndFlush(any(CashRegister.class)))
                .thenThrow(new DataIntegrityViolationException("uk_cash_register_code_company"));

        CashRegisterCodeAlreadyExistsException exception = assertThrows(
                CashRegisterCodeAlreadyExistsException.class,
                () -> service.create(new CreateCashRegisterRequest("Caixa novo", null))
        );

        assertEquals("CASH_REGISTER_CODE_ALREADY_EXISTS", exception.getCode());
    }

    @Test
    @DisplayName("Deve exigir filial no contexto ou na requisicao")
    void shouldRequireCompanyWhenNeitherContextNorRequestHasOne() {
        CreateCashRegisterRequest request = new CreateCashRegisterRequest("Caixa Principal", null);

        RequestContext.runWithin(RequestContext.empty(),
                () -> assertThrows(CompanyContextRequiredException.class, () -> service.create(request)));
        verify(repository, never()).saveAndFlush(any());
    }

    private void stubExistingCodes(String... codes) {
        when(repository.findCodesByCompanyIdAndCodeLike(COMPANY_ID, "CAIXA-%")).thenReturn(List.of(codes));
    }

    private void stubSave() {
        when(repository.saveAndFlush(any(CashRegister.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }
}
