package com.angeltlh31.lumora.security;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AuthRateLimitFilterTest {

    private static final class MutableClock extends Clock {
        private Instant now = Instant.parse("2026-01-01T00:00:00Z");

        @Override
        public ZoneOffset getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(java.time.ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return now;
        }

        void advance(Duration d) {
            now = now.plus(d);
        }
    }

    private final MutableClock clock = new MutableClock();
    private final AuthRateLimitFilter filter =
            new AuthRateLimitFilter(Map.of("/api/users/login", 3), clock);

    private int post(String path, String ip) throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", path);
        request.setRemoteAddr(ip);
        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilter(request, response, new MockFilterChain());
        return response.getStatus();
    }

    @Test
    void blocksAfterLimit_untilWindowResets() throws Exception {
        assertEquals(200, post("/api/users/login", "1.1.1.1"));
        assertEquals(200, post("/api/users/login", "1.1.1.1"));
        assertEquals(200, post("/api/users/login", "1.1.1.1"));
        assertEquals(429, post("/api/users/login", "1.1.1.1"));

        clock.advance(Duration.ofSeconds(61));

        assertEquals(200, post("/api/users/login", "1.1.1.1"));
    }

    @Test
    void countsEachIpSeparately() throws Exception {
        for (int i = 0; i < 3; i++) {
            post("/api/users/login", "1.1.1.1");
        }

        assertEquals(429, post("/api/users/login", "1.1.1.1"));
        assertEquals(200, post("/api/users/login", "2.2.2.2"));
    }

    @Test
    void ignoresOtherEndpoints() throws Exception {
        for (int i = 0; i < 10; i++) {
            assertEquals(200, post("/api/decks", "1.1.1.1"));
        }
    }
}
