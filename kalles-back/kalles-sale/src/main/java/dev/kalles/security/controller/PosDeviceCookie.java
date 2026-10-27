package dev.kalles.security.controller;

import org.springframework.http.ResponseCookie;

import java.time.Duration;

public final class PosDeviceCookie {

    public static final String NAME = "kalles_pos_token";
    static final Duration MAX_AGE = Duration.ofDays(400);

    private PosDeviceCookie() {
    }

    public static ResponseCookie issue(String deviceToken, boolean secure) {
        return ResponseCookie.from(NAME, deviceToken)
                .httpOnly(true)
                .secure(secure)
                .path("/")
                .maxAge(MAX_AGE)
                .sameSite("Strict")
                .build();
    }
}
