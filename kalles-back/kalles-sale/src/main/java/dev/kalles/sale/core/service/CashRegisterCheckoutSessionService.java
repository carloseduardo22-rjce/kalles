package dev.kalles.sale.core.service;

import dev.kalles.sale.cashregister.entity.CashRegisterSession;
import dev.kalles.sale.cashregister.repository.CashRegisterSessionRepository;
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
                .map(s -> new CashRegisterSessionAdapter(s));
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
    }
}
