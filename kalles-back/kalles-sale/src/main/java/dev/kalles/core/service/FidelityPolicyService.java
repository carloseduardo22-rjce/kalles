package dev.kalles.core.service;

import dev.kalles.core.dto.FidelityPolicyRequest;
import dev.kalles.core.dto.FidelityPolicyResponse;
import dev.kalles.core.entity.FidelityPolicy;
import dev.kalles.core.exception.NotFoundException;
import dev.kalles.core.repository.FidelityPolicyRepository;
import dev.kalles.security.context.CompanyContextHolder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FidelityPolicyService {

    private final FidelityPolicyRepository fidelityPolicyRepository;

    @Transactional
    public FidelityPolicyResponse create(FidelityPolicyRequest request) {
        UUID companyId = getCompanyId();
        fidelityPolicyRepository.deactivateAllByCompanyId(companyId);
        FidelityPolicy policy = new FidelityPolicy();
        policy.setCompanyId(companyId);
        policy.setObjectivePoints(request.objectivePoints());
        policy.setConfiguredDiscount(request.configuredDiscount());
        policy.setValuePoint(request.valuePoint());
        policy.setDiscountType(request.discountType());
        policy.setActive(true);
        policy.setCreatedAt(LocalDate.now());
        return FidelityPolicyResponse.from(fidelityPolicyRepository.save(policy));
    }

    @Transactional(readOnly = true)
    public FidelityPolicyResponse getActive() {
        return fidelityPolicyRepository.findFirstByCompanyIdAndActiveTrue(getCompanyId())
                .map(FidelityPolicyResponse::from)
                .orElseThrow(() -> new NotFoundException("Nenhuma política de fidelidade ativa encontrada."));
    }

    @Transactional(readOnly = true)
    public List<FidelityPolicyResponse> listAll() {
        return fidelityPolicyRepository.findAllByCompanyIdOrderByCreatedAtDesc(getCompanyId()).stream()
                .map(FidelityPolicyResponse::from)
                .toList();
    }

    private UUID getCompanyId() {
        UUID companyId = CompanyContextHolder.getCompanyId();
        if (companyId == null) {
            throw new IllegalStateException("Nenhuma filial selecionada no contexto da operação.");
        }
        return companyId;
    }
}
