package dev.kalles.shared.service;

import dev.kalles.shared.exception.NotFoundException;

import java.util.Optional;

public interface CheckoutSessionService {

    Optional<Session> findByToken(String sessionToken);

    default boolean isSessionOpen(String sessionToken) {
        return findByToken(sessionToken)
                .map(Session::isOpen)
                .orElse(false);
    }

    default Session getOpenSessionOrThrow(String sessionToken) {
        Session session = findByToken(sessionToken)
                .orElseThrow(() -> new NotFoundException("Sessão de caixa não encontrada: " + sessionToken));

        if (!session.isOpen()) {
            throw new IllegalStateException("Sessão de caixa não está aberta: " + sessionToken);
        }

        return session;
    }
}
