package dev.kalles.fidelity.service;

import dev.kalles.client.entity.Client;
import dev.kalles.client.repository.ClientRepository;
import dev.kalles.fidelity.dto.FidelityResponse;
import dev.kalles.fidelity.entity.Fidelity;
import dev.kalles.fidelity.entity.FidelityPolicy;
import dev.kalles.fidelity.enums.FidelityDiscountType;
import dev.kalles.fidelity.repository.FidelityPolicyRepository;
import dev.kalles.fidelity.repository.FidelityRepository;
import dev.kalles.fidelity.service.FidelityService;
import dev.kalles.security.context.CompanyContextHolder;
import dev.kalles.shared.exception.NotFoundException;
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
import java.time.LocalDateTime;
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
        activePolicy.setCreatedAt(LocalDateTime.now());
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
        f.setCreatedAt(LocalDateTime.now());
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
            fidelity.setCreatedAt(LocalDateTime.now().minusMonths(4));

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

            int earned = fidelityService.processCompletedSale(clientId, new BigDecimal("50.00"), BigDecimal.ZERO);

            assertEquals(50, earned);
            ArgumentCaptor<Fidelity> captor = ArgumentCaptor.forClass(Fidelity.class);
            verify(fidelityRepository).save(captor.capture());
            assertEquals(50, captor.getValue().getPoints());
        }

        @Test
        @DisplayName("Deve conceder desconto e preservar pontos excedentes ao atingir o objetivo")
        void shouldGrantDiscountAndKeepExcessPointsWhenObjectiveReached() {
            Fidelity fidelity = buildFidelity(80, BigDecimal.ZERO);
            when(fidelityRepository.findByClientId(clientId)).thenReturn(Optional.of(fidelity));
            when(fidelityRepository.save(any(Fidelity.class))).thenReturn(fidelity);

            fidelityService.processCompletedSale(clientId, new BigDecimal("30.00"), BigDecimal.ZERO);

            ArgumentCaptor<Fidelity> captor = ArgumentCaptor.forClass(Fidelity.class);
            verify(fidelityRepository).save(captor.capture());
            Fidelity saved = captor.getValue();
            // 80 + 30 = 110 pontos; objetivo 100 → 1 recompensa e 10 pontos preservados
            assertEquals(10, saved.getPoints());
            assertEquals(new BigDecimal("20.00"), saved.getAvailableDiscount());
        }

        @Test
        @DisplayName("Deve converter múltiplas recompensas quando pontos acumulados superam vários objetivos")
        void shouldGrantMultipleRewardsWhenPointsExceedMultipleObjectives() {
            Fidelity fidelity = buildFidelity(150, BigDecimal.ZERO);
            when(fidelityRepository.findByClientId(clientId)).thenReturn(Optional.of(fidelity));
            when(fidelityRepository.save(any(Fidelity.class))).thenReturn(fidelity);

            fidelityService.processCompletedSale(clientId, new BigDecimal("100.00"), BigDecimal.ZERO);

            ArgumentCaptor<Fidelity> captor = ArgumentCaptor.forClass(Fidelity.class);
            verify(fidelityRepository).save(captor.capture());
            Fidelity saved = captor.getValue();
            // 150 + 100 = 250 pontos; objetivo 100 → 2 recompensas e 50 pontos preservados
            assertEquals(50, saved.getPoints());
            assertEquals(new BigDecimal("40.00"), saved.getAvailableDiscount());
        }

        @Test
        @DisplayName("Deve consumir o desconto aplicado na venda concluída (FIXED)")
        void shouldConsumeAppliedFixedDiscountOnCompletion() {
            Fidelity fidelity = buildFidelity(0, new BigDecimal("20.00"));
            when(fidelityRepository.findByClientId(clientId)).thenReturn(Optional.of(fidelity));
            when(fidelityRepository.save(any(Fidelity.class))).thenReturn(fidelity);

            fidelityService.processCompletedSale(clientId, new BigDecimal("50.00"), new BigDecimal("20.00"));

            ArgumentCaptor<Fidelity> captor = ArgumentCaptor.forClass(Fidelity.class);
            verify(fidelityRepository).save(captor.capture());
            assertEquals(0, BigDecimal.ZERO.compareTo(captor.getValue().getAvailableDiscount()));
        }

        @Test
        @DisplayName("Deve zerar o percentual disponível quando desconto percentual foi usado")
        void shouldZeroPercentageWhenPercentageDiscountWasUsed() {
            activePolicy.setDiscountType(FidelityDiscountType.PERCENTAGE);
            activePolicy.setConfiguredDiscount(new BigDecimal("10.00"));
            Fidelity fidelity = buildFidelity(0, new BigDecimal("10.00"));
            when(fidelityRepository.findByClientId(clientId)).thenReturn(Optional.of(fidelity));
            when(fidelityRepository.save(any(Fidelity.class))).thenReturn(fidelity);

            fidelityService.processCompletedSale(clientId, new BigDecimal("80.00"), new BigDecimal("8.00"));

            ArgumentCaptor<Fidelity> captor = ArgumentCaptor.forClass(Fidelity.class);
            verify(fidelityRepository).save(captor.capture());
            assertEquals(BigDecimal.ZERO, captor.getValue().getAvailableDiscount());
        }

        @Test
        @DisplayName("Deve retornar zero pontos quando cliente não está no programa")
        void shouldReturnZeroPointsWhenClientHasNoFidelity() {
            when(fidelityRepository.findByClientId(clientId)).thenReturn(Optional.empty());

            int earned = fidelityService.processCompletedSale(clientId, new BigDecimal("50.00"), BigDecimal.ZERO);

            assertEquals(0, earned);
            verify(fidelityRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve ganhar pontos sobre o total original mesmo quando desconto foi aplicado")
        void shouldEarnPointsOnOriginalTotalEvenWhenDiscountWasApplied() {
            Fidelity fidelity = buildFidelity(0, BigDecimal.ZERO);
            when(fidelityRepository.findByClientId(clientId)).thenReturn(Optional.of(fidelity));
            when(fidelityRepository.save(any(Fidelity.class))).thenReturn(fidelity);

            int earned = fidelityService.processCompletedSale(clientId, new BigDecimal("100.00"), BigDecimal.ZERO);

            assertEquals(100, earned);
        }
    }

    @Nested
    @DisplayName("Cálculo de desconto de fidelidade (sem consumo)")
    class CalculateDiscount {

        @Test
        @DisplayName("Deve calcular desconto integral sem consumir o saldo do cliente")
        void shouldCalculateFullDiscountWithoutConsumingBalance() {
            Fidelity fidelity = buildFidelity(0, new BigDecimal("20.00"));
            when(fidelityRepository.findByClientId(clientId)).thenReturn(Optional.of(fidelity));

            BigDecimal applied = fidelityService.calculateDiscount(clientId, new BigDecimal("80.00"));

            assertEquals(new BigDecimal("20.00"), applied);
            // Saldo intacto: consumo só acontece na conclusão da venda
            assertEquals(new BigDecimal("20.00"), fidelity.getAvailableDiscount());
            verify(fidelityRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve limitar o desconto ao total da venda preservando o saldo restante")
        void shouldCapDiscountAtSaleTotal() {
            Fidelity fidelity = buildFidelity(0, new BigDecimal("50.00"));
            when(fidelityRepository.findByClientId(clientId)).thenReturn(Optional.of(fidelity));

            BigDecimal applied = fidelityService.calculateDiscount(clientId, new BigDecimal("20.00"));

            assertEquals(new BigDecimal("20.00"), applied);
            assertEquals(new BigDecimal("50.00"), fidelity.getAvailableDiscount());
        }

        @Test
        @DisplayName("Recalcular (aplicar duas vezes) retorna o mesmo valor sem dupla cobrança")
        void shouldBeIdempotentWhenCalculatedTwice() {
            Fidelity fidelity = buildFidelity(0, new BigDecimal("20.00"));
            when(fidelityRepository.findByClientId(clientId)).thenReturn(Optional.of(fidelity));

            BigDecimal first = fidelityService.calculateDiscount(clientId, new BigDecimal("80.00"));
            BigDecimal second = fidelityService.calculateDiscount(clientId, new BigDecimal("80.00"));

            assertEquals(first, second);
            assertEquals(new BigDecimal("20.00"), fidelity.getAvailableDiscount());
        }

        @Test
        @DisplayName("Deve retornar zero quando cliente não está no programa de fidelidade")
        void shouldReturnZeroWhenClientHasNoFidelity() {
            when(fidelityRepository.findByClientId(clientId)).thenReturn(Optional.empty());

            BigDecimal applied = fidelityService.calculateDiscount(clientId, new BigDecimal("50.00"));

            assertEquals(BigDecimal.ZERO, applied);
            verify(fidelityRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve retornar zero quando desconto disponível é zero")
        void shouldReturnZeroWhenAvailableDiscountIsZero() {
            Fidelity fidelity = buildFidelity(50, BigDecimal.ZERO);
            when(fidelityRepository.findByClientId(clientId)).thenReturn(Optional.of(fidelity));

            BigDecimal applied = fidelityService.calculateDiscount(clientId, new BigDecimal("50.00"));

            assertEquals(BigDecimal.ZERO, applied);
            verify(fidelityRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve calcular desconto percentual sobre o total sem zerar o prêmio")
        void shouldCalculatePercentageDiscountWithoutConsumingReward() {
            activePolicy.setDiscountType(FidelityDiscountType.PERCENTAGE);
            activePolicy.setConfiguredDiscount(new BigDecimal("10.00"));
            Fidelity fidelity = buildFidelity(0, new BigDecimal("10.00"));
            when(fidelityRepository.findByClientId(clientId)).thenReturn(Optional.of(fidelity));

            BigDecimal applied = fidelityService.calculateDiscount(clientId, new BigDecimal("80.00"));

            assertEquals(new BigDecimal("8.00"), applied);
            assertEquals(new BigDecimal("10.00"), fidelity.getAvailableDiscount());
        }

        @Test
        @DisplayName("Deve retornar zero e marcar como expirado quando a fidelidade venceu")
        void shouldReturnZeroWhenFidelityIsExpired() {
            Fidelity fidelity = buildFidelity(0, new BigDecimal("20.00"));
            fidelity.setCreatedAt(LocalDateTime.now().minusMonths(4));
            when(fidelityRepository.findByClientId(clientId)).thenReturn(Optional.of(fidelity));
            when(fidelityRepository.save(any(Fidelity.class))).thenReturn(fidelity);

            BigDecimal applied = fidelityService.calculateDiscount(clientId, new BigDecimal("80.00"));

            assertEquals(BigDecimal.ZERO, applied);
            assertTrue(fidelity.isExpired());
        }
    }
}
