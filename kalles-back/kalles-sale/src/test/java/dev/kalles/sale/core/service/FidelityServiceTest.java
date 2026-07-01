package dev.kalles.sale.core.service;

import dev.kalles.sale.core.dto.FidelityResponse;
import dev.kalles.sale.core.entity.Client;
import dev.kalles.sale.core.entity.Fidelity;
import dev.kalles.sale.core.entity.FidelityPolicy;
import dev.kalles.sale.core.enums.fidelity.FidelityDiscountType;
import dev.kalles.sale.core.exception.NotFoundException;
import dev.kalles.sale.core.repository.ClientRepository;
import dev.kalles.sale.core.repository.FidelityPolicyRepository;
import dev.kalles.sale.core.repository.FidelityRepository;
import dev.kalles.sale.security.context.CompanyContextHolder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("FidelityService - Serviço de Fidelidade")
class FidelityServiceTest {

    private static final UUID COMPANY_ID = UUID.fromString("e28a38a0-2f22-4a00-9e6b-67e9f3b5c65f");

    @Mock
    private FidelityRepository fidelityRepository;

    @Mock
    private FidelityPolicyRepository fidelityPolicyRepository;

    @Mock
    private ClientRepository clientRepository;

    @InjectMocks
    private FidelityService fidelityService;

    private UUID clientId;
    private Client client;
    private FidelityPolicy activePolicy;

    @BeforeEach
    void setUp() {
        CompanyContextHolder.setCompanyId(COMPANY_ID);
        clientId = UUID.randomUUID();
        client = new Client();
        client.setId(clientId);
        client.setName("Ana Paula");
        client.setCompanyId(COMPANY_ID);

        activePolicy = new FidelityPolicy();
        activePolicy.setId(UUID.randomUUID());
        activePolicy.setObjectivePoints(100);
        activePolicy.setConfiguredDiscount(new BigDecimal("20.00"));
        activePolicy.setValuePoint(1);
        activePolicy.setDiscountType(FidelityDiscountType.FIXED);
        activePolicy.setActive(true);
        activePolicy.setCreatedAt(LocalDate.now());
    }

    @AfterEach
    void tearDown() {
        CompanyContextHolder.clear();
    }

    private Fidelity buildFidelity(int points, BigDecimal discount) {
        Fidelity f = new Fidelity();
        f.setId(UUID.randomUUID());
        f.setClient(client);
        f.setPolicy(activePolicy);
        f.setPoints(points);
        f.setAvailableDiscount(discount);
        f.setCreatedAt(LocalDate.now());
        f.setExpired(false);
        return f;
    }

    @Nested
    @DisplayName("Inscrição do cliente no programa")
    class EnrollClient {

        @Test
        @DisplayName("Deve inscrever o cliente com sucesso")
        void shouldEnrollClientSuccessfully() {
            Fidelity saved = buildFidelity(0, BigDecimal.ZERO);

            when(fidelityRepository.existsByClientId(clientId)).thenReturn(false);
            when(fidelityPolicyRepository.findFirstByCompanyIdAndActiveTrue(COMPANY_ID)).thenReturn(Optional.of(activePolicy));
            when(clientRepository.findByIdAndCompanyId(clientId, COMPANY_ID)).thenReturn(Optional.of(client));
            when(fidelityRepository.save(any(Fidelity.class))).thenReturn(saved);

            FidelityResponse response = fidelityService.enrollClient(clientId);

            assertNotNull(response);
            assertEquals(clientId, response.clientId());
            assertEquals(0, response.points());
            assertEquals(BigDecimal.ZERO, response.availableDiscount());
            verify(fidelityRepository).save(any(Fidelity.class));
        }

        @Test
        @DisplayName("Deve lançar exceção quando cliente já está inscrito")
        void shouldThrowWhenClientAlreadyEnrolled() {
            when(fidelityRepository.existsByClientId(clientId)).thenReturn(true);

            assertThrows(IllegalArgumentException.class, () -> fidelityService.enrollClient(clientId));
            verify(fidelityRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar exceção quando não existe política ativa")
        void shouldThrowWhenNoActivePolicyExists() {
            when(fidelityRepository.existsByClientId(clientId)).thenReturn(false);
            when(fidelityPolicyRepository.findFirstByCompanyIdAndActiveTrue(COMPANY_ID)).thenReturn(Optional.empty());

            assertThrows(IllegalStateException.class, () -> fidelityService.enrollClient(clientId));
            verify(fidelityRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar exceção quando cliente não existe")
        void shouldThrowNotFoundWhenClientDoesNotExist() {
            when(fidelityRepository.existsByClientId(clientId)).thenReturn(false);
            when(fidelityPolicyRepository.findFirstByCompanyIdAndActiveTrue(COMPANY_ID)).thenReturn(Optional.of(activePolicy));
            when(clientRepository.findByIdAndCompanyId(clientId, COMPANY_ID)).thenReturn(Optional.empty());

            assertThrows(NotFoundException.class, () -> fidelityService.enrollClient(clientId));
        }
    }

    @Nested
    @DisplayName("Consulta de fidelidade por cliente")
    class GetByClientId {

        @Test
        @DisplayName("Deve retornar a fidelidade do cliente existente")
        void shouldReturnFidelityForExistingClient() {
            Fidelity fidelity = buildFidelity(50, BigDecimal.ZERO);
            when(clientRepository.findByIdAndCompanyId(clientId, COMPANY_ID)).thenReturn(Optional.of(client));
            when(fidelityRepository.findByClientId(clientId)).thenReturn(Optional.of(fidelity));

            FidelityResponse response = fidelityService.getByClientId(clientId);

            assertEquals(clientId, response.clientId());
            assertEquals(50, response.points());
            assertEquals(FidelityDiscountType.FIXED, response.discountType());
        }

        @Test
        @DisplayName("Deve lançar exceção quando cliente não está no programa")
        void shouldThrowNotFoundWhenClientHasNoFidelity() {
            when(clientRepository.findByIdAndCompanyId(clientId, COMPANY_ID)).thenReturn(Optional.of(client));
            when(fidelityRepository.findByClientId(clientId)).thenReturn(Optional.empty());

            assertThrows(NotFoundException.class, () -> fidelityService.getByClientId(clientId));
        }

        @Test
        @DisplayName("Deve bloquear consulta quando cliente nao pertence a filial ativa")
        void shouldBlockLookupWhenClientDoesNotBelongToActiveCompany() {
            when(clientRepository.findByIdAndCompanyId(clientId, COMPANY_ID)).thenReturn(Optional.empty());

            assertThrows(NotFoundException.class, () -> fidelityService.getByClientId(clientId));
            verify(fidelityRepository, never()).findByClientId(clientId);
        }

        @Test
        @DisplayName("Deve marcar como expirado quando criado há mais de 3 meses")
        void shouldMarkAsExpiredWhenCreatedAtIsMoreThanThreeMonthsAgo() {
            Fidelity fidelity = buildFidelity(0, BigDecimal.ZERO);
            fidelity.setCreatedAt(LocalDate.now().minusMonths(4));

            when(clientRepository.findByIdAndCompanyId(clientId, COMPANY_ID)).thenReturn(Optional.of(client));
            when(fidelityRepository.findByClientId(clientId)).thenReturn(Optional.of(fidelity));
            when(fidelityRepository.save(any(Fidelity.class))).thenReturn(fidelity);

            FidelityResponse response = fidelityService.getByClientId(clientId);

            assertTrue(response.expired());
            verify(fidelityRepository).save(fidelity);
        }
    }

    @Nested
    @DisplayName("Processamento de venda concluída")
    class ProcessCompletedSale {

        @Test
        @DisplayName("Deve acumular pontos com base no total da venda")
        void shouldAddPointsBasedOnSaleTotal() {
            Fidelity fidelity = buildFidelity(0, BigDecimal.ZERO);
            when(fidelityRepository.findByClientId(clientId)).thenReturn(Optional.of(fidelity));
            when(fidelityRepository.save(any(Fidelity.class))).thenReturn(fidelity);

            int earned = fidelityService.processCompletedSale(clientId, new BigDecimal("50.00"));

            assertEquals(50, earned);
            ArgumentCaptor<Fidelity> captor = ArgumentCaptor.forClass(Fidelity.class);
            verify(fidelityRepository).save(captor.capture());
            assertEquals(50, captor.getValue().getPoints());
        }

        @Test
        @DisplayName("Deve definir desconto disponível e zerar pontos ao atingir o objetivo")
        void shouldSetAvailableDiscountAndResetPointsWhenObjectiveReached() {
            Fidelity fidelity = buildFidelity(80, BigDecimal.ZERO);
            when(fidelityRepository.findByClientId(clientId)).thenReturn(Optional.of(fidelity));
            when(fidelityRepository.save(any(Fidelity.class))).thenReturn(fidelity);

            fidelityService.processCompletedSale(clientId, new BigDecimal("30.00"));

            ArgumentCaptor<Fidelity> captor = ArgumentCaptor.forClass(Fidelity.class);
            verify(fidelityRepository).save(captor.capture());
            Fidelity saved = captor.getValue();
            assertEquals(0, saved.getPoints());
            assertEquals(new BigDecimal("20.00"), saved.getAvailableDiscount());
        }

        @Test
        @DisplayName("Deve retornar zero pontos quando cliente não está no programa")
        void shouldReturnZeroPointsWhenClientHasNoFidelity() {
            when(fidelityRepository.findByClientId(clientId)).thenReturn(Optional.empty());

            int earned = fidelityService.processCompletedSale(clientId, new BigDecimal("50.00"));

            assertEquals(0, earned);
            verify(fidelityRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve ganhar pontos sobre o total original mesmo quando desconto foi aplicado")
        void shouldEarnPointsOnOriginalTotalEvenWhenDiscountWasApplied() {
            Fidelity fidelity = buildFidelity(0, BigDecimal.ZERO);
            when(fidelityRepository.findByClientId(clientId)).thenReturn(Optional.of(fidelity));
            when(fidelityRepository.save(any(Fidelity.class))).thenReturn(fidelity);

            int earned = fidelityService.processCompletedSale(clientId, new BigDecimal("100.00"));

            assertEquals(100, earned);
        }
    }

    @Nested
    @DisplayName("Aplicação de desconto de fidelidade")
    class ApplyDiscount {

        @Test
        @DisplayName("Deve aplicar desconto integral quando total da venda supera o desconto disponível")
        void shouldApplyFullDiscountWhenSaleTotalExceedsAvailableDiscount() {
            Fidelity fidelity = buildFidelity(0, new BigDecimal("20.00"));
            when(fidelityRepository.findByClientId(clientId)).thenReturn(Optional.of(fidelity));
            when(fidelityRepository.save(any(Fidelity.class))).thenReturn(fidelity);

            BigDecimal applied = fidelityService.applyDiscount(clientId, new BigDecimal("80.00"));

            assertEquals(new BigDecimal("20.00"), applied);
            ArgumentCaptor<Fidelity> captor = ArgumentCaptor.forClass(Fidelity.class);
            verify(fidelityRepository).save(captor.capture());
            assertEquals(0, BigDecimal.ZERO.compareTo(captor.getValue().getAvailableDiscount()));
        }

        @Test
        @DisplayName("Deve aplicar desconto parcial e preservar o saldo quando desconto supera o total da venda")
        void shouldApplyPartialDiscountAndPreserveRemainderWhenDiscountExceedsSaleTotal() {
            Fidelity fidelity = buildFidelity(0, new BigDecimal("50.00"));
            when(fidelityRepository.findByClientId(clientId)).thenReturn(Optional.of(fidelity));
            when(fidelityRepository.save(any(Fidelity.class))).thenReturn(fidelity);

            BigDecimal applied = fidelityService.applyDiscount(clientId, new BigDecimal("20.00"));

            assertEquals(new BigDecimal("20.00"), applied);
            ArgumentCaptor<Fidelity> captor = ArgumentCaptor.forClass(Fidelity.class);
            verify(fidelityRepository).save(captor.capture());
            assertEquals(new BigDecimal("30.00"), captor.getValue().getAvailableDiscount());
        }

        @Test
        @DisplayName("Deve retornar zero quando cliente não está no programa de fidelidade")
        void shouldReturnZeroWhenClientHasNoFidelity() {
            when(fidelityRepository.findByClientId(clientId)).thenReturn(Optional.empty());

            BigDecimal applied = fidelityService.applyDiscount(clientId, new BigDecimal("50.00"));

            assertEquals(BigDecimal.ZERO, applied);
            verify(fidelityRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve retornar zero quando desconto disponível é zero")
        void shouldReturnZeroWhenAvailableDiscountIsZero() {
            Fidelity fidelity = buildFidelity(50, BigDecimal.ZERO);
            when(fidelityRepository.findByClientId(clientId)).thenReturn(Optional.of(fidelity));

            BigDecimal applied = fidelityService.applyDiscount(clientId, new BigDecimal("50.00"));

            assertEquals(BigDecimal.ZERO, applied);
            verify(fidelityRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve aplicar desconto percentual sobre o total da venda e consumir o premio")
        void shouldApplyPercentageDiscountAndConsumeReward() {
            activePolicy.setDiscountType(FidelityDiscountType.PERCENTAGE);
            activePolicy.setConfiguredDiscount(new BigDecimal("10.00"));
            Fidelity fidelity = buildFidelity(0, new BigDecimal("10.00"));
            when(fidelityRepository.findByClientId(clientId)).thenReturn(Optional.of(fidelity));
            when(fidelityRepository.save(any(Fidelity.class))).thenReturn(fidelity);

            BigDecimal applied = fidelityService.applyDiscount(clientId, new BigDecimal("80.00"));

            assertEquals(new BigDecimal("8.00"), applied);
            ArgumentCaptor<Fidelity> captor = ArgumentCaptor.forClass(Fidelity.class);
            verify(fidelityRepository).save(captor.capture());
            assertEquals(BigDecimal.ZERO, captor.getValue().getAvailableDiscount());
        }
    }

    @Nested
    @DisplayName("Estorno de pontos e desconto em cancelamento")
    class RollbackSale {

        @Test
        @DisplayName("Deve estornar pontos na elimínação da venda")
        void shouldRollbackPointsOnSaleCancellation() {
            Fidelity fidelity = buildFidelity(30, BigDecimal.ZERO);
            when(fidelityRepository.findByClientId(clientId)).thenReturn(Optional.of(fidelity));
            when(fidelityRepository.save(any(Fidelity.class))).thenReturn(fidelity);

            fidelityService.rollbackSale(clientId, BigDecimal.ZERO, 30);

            ArgumentCaptor<Fidelity> captor = ArgumentCaptor.forClass(Fidelity.class);
            verify(fidelityRepository).save(captor.capture());
            assertEquals(0, captor.getValue().getPoints());
        }

        @Test
        @DisplayName("Deve restituir desconto na elimínação da venda")
        void shouldRollbackDiscountOnSaleCancellation() {
            Fidelity fidelity = buildFidelity(0, new BigDecimal("30.00"));
            when(fidelityRepository.findByClientId(clientId)).thenReturn(Optional.of(fidelity));
            when(fidelityRepository.save(any(Fidelity.class))).thenReturn(fidelity);

            fidelityService.rollbackSale(clientId, new BigDecimal("20.00"), 0);

            ArgumentCaptor<Fidelity> captor = ArgumentCaptor.forClass(Fidelity.class);
            verify(fidelityRepository).save(captor.capture());
            assertEquals(new BigDecimal("50.00"), captor.getValue().getAvailableDiscount());
        }

        @Test
        @DisplayName("Deve limitar pontos a zero quando estorno excede o saldo atual")
        void shouldClampPointsToZeroWhenRollbackExceedsCurrentPoints() {
            Fidelity fidelity = buildFidelity(10, BigDecimal.ZERO);
            when(fidelityRepository.findByClientId(clientId)).thenReturn(Optional.of(fidelity));
            when(fidelityRepository.save(any(Fidelity.class))).thenReturn(fidelity);

            fidelityService.rollbackSale(clientId, BigDecimal.ZERO, 50);

            ArgumentCaptor<Fidelity> captor = ArgumentCaptor.forClass(Fidelity.class);
            verify(fidelityRepository).save(captor.capture());
            assertEquals(0, captor.getValue().getPoints());
        }

        @Test
        @DisplayName("Não deve fazer nada quando cliente não está no programa de fidelidade")
        void shouldSkipRollbackWhenClientHasNoFidelity() {
            when(fidelityRepository.findByClientId(clientId)).thenReturn(Optional.empty());

            assertDoesNotThrow(() -> fidelityService.rollbackSale(clientId, new BigDecimal("20.00"), 30));
            verify(fidelityRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve recompor premio percentual com base no subtotal original")
        void shouldRollbackPercentageRewardUsingOriginalSubtotal() {
            activePolicy.setDiscountType(FidelityDiscountType.PERCENTAGE);
            activePolicy.setConfiguredDiscount(new BigDecimal("10.00"));
            Fidelity fidelity = buildFidelity(0, BigDecimal.ZERO);
            when(fidelityRepository.findByClientId(clientId)).thenReturn(Optional.of(fidelity));
            when(fidelityRepository.save(any(Fidelity.class))).thenReturn(fidelity);

            fidelityService.rollbackSale(
                    clientId,
                    new BigDecimal("8.00"),
                    0,
                    new BigDecimal("80.00"));

            ArgumentCaptor<Fidelity> captor = ArgumentCaptor.forClass(Fidelity.class);
            verify(fidelityRepository).save(captor.capture());
            assertEquals(new BigDecimal("10.00"), captor.getValue().getAvailableDiscount());
        }
    }
}
