package dev.kalles.security.service;

import dev.kalles.security.entity.Account;

public record RefreshTokenPrincipal(
        Account account
) {
}
