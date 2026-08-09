package dev.kalles.fidelity.service;

import dev.kalles.client.entity.Client;
import dev.kalles.client.repository.ClientRepository;
import dev.kalles.fidelity.dto.FidelityResponse;
import dev.kalles.fidelity.entity.Fidelity;
import dev.kalles.fidelity.entity.FidelityPolicy;
import dev.kalles.fidelity.repository.FidelityPolicyRepository;
import dev.kalles.fidelity.repository.FidelityRepository;
import dev.kalles.security.context.CompanyContextHolder;
import dev.kalles.shared.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FidelityService {

    private final FidelityRepository fidelityRepository;
    private final FidelityPolicyRepository fidelityPolicyRepository;
    private final ClientRepository clientRepository;

    @Transactional
    public FidelityResponse enrollClient(UUID clientId) {
        if (fidelityRepository.existsByClientId(clientId)) {
            throw new IllegalArgumentException("Cliente já está inserido no programa de fidelidade.");
        }
        UUID companyId = getCompanyId();
        FidelityPolicy policy = fidelityPolicyRepository.findFirstByCompanyIdAndActiveTrue(companyId)
                .orElseThrow(() -> new IllegalStateException("Nenhuma política de fidelidade ativa encontrada para esta filial."));
        Client client = clientRepository.findByIdAndCompanyId(clientId, companyId)
                .orElseThrow(() -> new NotFoundException("Cliente não encontrado com o id: " + clientId));
        Fidelity fidelity = new Fidelity();
        fidelity.setClient(client);
        fidelity.setPolicy(policy);
        fidelity.setPoints(0);
        fidelity.setAvailableDiscount(BigDecimal.ZERO);
        fidelity.setCreatedAt(LocalDate.now());
        return FidelityResponse.from(fidelityRepository.save(fidelity));
    }

    @Transactional
    public FidelityResponse getByClientId(UUID clientId) {
        UUID companyId = getCompanyId();
        clientRepository.findByIdAndCompanyId(clientId, companyId)
                .orElseThrow(() -> new NotFoundException("Cliente não encontrado com o id: " + clientId));

        Fidelity fidelity = fidelityRepository.findByClientId(clientId)
                .orElseThrow(() -> new NotFoundException("Cliente não está no programa de fidelidade."));
        checkAndMarkExpired(fidelity);
        return FidelityResponse.from(fidelity);
    }

    /**
     * Calcula o desconto de fidelidade aplicável à venda SEM consumir o saldo do
     * cliente. O consumo acontece apenas em processCompletedSale: venda abandonada
     * ou cancelada não queima o benefício, e reaplicar apenas recalcula.
     */
    @Transactional
    public BigDecimal calculateDiscount(UUID clientId, BigDecimal saleTotal) {
        Optional<Fidelity> optional = fidelityRepository.findByClientId(clientId);
        if (optional.isEmpty()) return BigDecimal.ZERO;
        Fidelity fidelity = optional.get();
        checkAndMarkExpired(fidelity);
        if (fidelity.isExpired()) return BigDecimal.ZERO;
        if (fidelity.getAvailableDiscount().compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;
        return fidelity.previewDiscount(saleTotal);
    }

    /**
     * Consolida a fidelidade na conclusão da venda: consome o desconto que foi
     * efetivamente aplicado e acumula os pontos sobre o subtotal original.
     */
    @Transactional
    public int processCompletedSale(UUID clientId, BigDecimal originalTotal, BigDecimal appliedDiscount) {
        Optional<Fidelity> optional = fidelityRepository.findByClientId(clientId);
        if (optional.isEmpty()) return 0;
        Fidelity fidelity = optional.get();
        if (appliedDiscount != null && appliedDiscount.compareTo(BigDecimal.ZERO) > 0) {
            fidelity.consumeAppliedDiscount(appliedDiscount);
        }
        int earned = fidelity.calculatePoints(originalTotal);
        fidelity.checkObjectivePoints();
        fidelityRepository.save(fidelity);
        return earned;
    }

    private void checkAndMarkExpired(Fidelity fidelity) {
        if (!fidelity.isExpired() && fidelity.isActuallyExpired()) {
            fidelity.setExpired(true);
            fidelityRepository.save(fidelity);
        }
    }

    private UUID getCompanyId() {
        UUID companyId = CompanyContextHolder.getCompanyId();
        if (companyId == null) {
            throw new IllegalStateException("Nenhuma filial selecionada no contexto da operação.");
        }
        return companyId;
    }
}
