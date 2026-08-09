package dev.kalles.cashregister.service;

import dev.kalles.cashregister.entity.CashRegisterSession;
import dev.kalles.cashregister.repository.CashRegisterSessionRepository;
import dev.kalles.shared.service.CheckoutSessionService;
import dev.kalles.shared.service.Session;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class CashRegisterCheckoutSessionService implements CheckoutSessionService {

    private final CashRegisterSessionRepository sessionRepository;

    public CashRegisterCheckoutSessionService(CashRegisterSessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
    }

    @Override
    public Optional<Session> findByToken(String sessionToken) {
        if (sessionToken == null) {
            return Optional.empty();
        }

        UUID id;
        try {
            id = UUID.fromString(sessionToken);
        } catch (IllegalArgumentException ex) {
            return Optional.empty();
        }

        return sessionRepository.findById(id)
                .filter(CashRegisterSession::isOpen)
                .map(CashRegisterSessionAdapter::new);
    }

    private static final class CashRegisterSessionAdapter implements Session {
        private final CashRegisterSession session;

        private CashRegisterSessionAdapter(CashRegisterSession session) {
            this.session = session;
        }

        @Override
        public String getToken() {
            return session.getId().toString();
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
