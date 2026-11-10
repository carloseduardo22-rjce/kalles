package dev.kalles.cashregister.service;

import dev.kalles.cashregister.entity.CashRegisterSession;
import dev.kalles.cashregister.repository.CashRegisterSessionRepository;
import dev.kalles.shared.exception.NotFoundException;
import dev.kalles.shared.service.CheckoutSessionService;
import dev.kalles.shared.service.Session;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class CashRegisterCheckoutSessionService implements CheckoutSessionService {

    private final CashRegisterSessionRepository sessionRepository;
    private final PairedDeviceSessionGuard pairedDeviceSessionGuard;

    public CashRegisterCheckoutSessionService(
            CashRegisterSessionRepository sessionRepository,
            PairedDeviceSessionGuard pairedDeviceSessionGuard
    ) {
        this.sessionRepository = sessionRepository;
        this.pairedDeviceSessionGuard = pairedDeviceSessionGuard;
    }

    @Override
    public Optional<Session> findByToken(String sessionToken) {
        return findOpenSession(sessionToken).map(CashRegisterSessionAdapter::new);
    }

    @Override
    public Session getOpenSessionOrThrow(String sessionToken) {
        CashRegisterSession session = findOpenSession(sessionToken)
                .orElseThrow(() -> new NotFoundException("Sessão de caixa aberta não encontrada: " + sessionToken));
        pairedDeviceSessionGuard.ensureCanOperate(session.getCashRegister());
        return new CashRegisterSessionAdapter(session);
    }

    private Optional<CashRegisterSession> findOpenSession(String sessionToken) {
        if (sessionToken == null) {
            return Optional.empty();
        }

        UUID id;
        try {
            id = UUID.fromString(sessionToken);
        } catch (IllegalArgumentException ex) {
            return Optional.empty();
        }

        return sessionRepository.findById(id).filter(CashRegisterSession::isOpen);
    }

    private static final class CashRegisterSessionAdapter implements Session {
        private final CashRegisterSession session;

        private CashRegisterSessionAdapter(CashRegisterSession session) {
            this.session = session;
        }

        @Override
        public UUID getId() {
            return session.getId();
        }

        @Override
        public boolean isOpen() {
            return session.isOpen();
        }

        @Override
        public boolean allowsElectronicPayments() {
            return session.allowsElectronicPayments();
        }
    }
}
