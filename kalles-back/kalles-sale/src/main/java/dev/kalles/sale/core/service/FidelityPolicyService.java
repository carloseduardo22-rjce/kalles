package dev.kalles.sale.core.service;

import dev.kalles.sale.core.dto.FidelityPolicyRequest;
import dev.kalles.sale.core.dto.FidelityPolicyResponse;
import dev.kalles.sale.core.entity.FidelityPolicy;
import dev.kalles.sale.core.exception.NotFoundException;
import dev.kalles.sale.core.repository.FidelityPolicyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FidelityPolicyService {

    private final FidelityPolicyRepository fidelityPolicyRepository;

    @Transactional
    public FidelityPolicyResponse create(FidelityPolicyRequest request) {
        fidelityPolicyRepository.deactivateAll();
        FidelityPolicy policy = new FidelityPolicy();
        policy.setObjectivePoints(request.objectivePoints());
        policy.setConfiguredDiscount(request.configuredDiscount());
        policy.setValuePoint(request.valuePoint());
        policy.setActive(true);
        policy.setCreatedAt(LocalDate.now());
        return FidelityPolicyResponse.from(fidelityPolicyRepository.save(policy));
    }

    @Transactional(readOnly = true)
    public FidelityPolicyResponse getActive() {
        return fidelityPolicyRepository.findFirstByActiveTrue()
                .map(FidelityPolicyResponse::from)
                .orElseThrow(() -> new NotFoundException("Nenhuma política de fidelidade ativa encontrada."));
    }

    @Transactional(readOnly = true)
    public List<FidelityPolicyResponse> listAll() {
        return fidelityPolicyRepository.findAll().stream()
                .map(FidelityPolicyResponse::from)
                .toList();
    }
}
