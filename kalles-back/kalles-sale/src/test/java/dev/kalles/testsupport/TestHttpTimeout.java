package dev.kalles.testsupport;

import java.time.Duration;

public final class TestHttpTimeout {

    public static final Duration REQUEST = Duration.ofSeconds(
            Long.getLong("kalles.test.http.timeout-seconds", 60L));

    public static final Duration CONNECT = Duration.ofSeconds(15);

    private TestHttpTimeout() {
    }
}
