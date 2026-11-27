package com.angeltlh31.lumora.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Clock;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class AuthRateLimitFilter extends OncePerRequestFilter {
    private static final long WINDOW_MS = 60_000;
    private static final int MAX_TRACKED_KEYS = 100_000;

    private final Map<String, Integer> limits;
    private final Map<String, Window> windows = new ConcurrentHashMap<>();
    private final Clock clock;

    @Autowired
    public AuthRateLimitFilter(@Value("${auth.rate-limit.login-per-minute:10}") int loginPerMinute,
                               @Value("${auth.rate-limit.register-per-minute:5}") int registerPerMinute,
                               @Value("${auth.rate-limit.refresh-per-minute:60}") int refreshPerMinute,
                               @Value("${auth.rate-limit.email-per-minute:3}") int emailPerMinute,
                               @Value("${auth.rate-limit.verify-per-minute:20}") int verifyPerMinute) {
        this(Map.of(
                "/api/users/login", loginPerMinute,
                "/api/users/register", registerPerMinute,
                "/api/users/refresh", refreshPerMinute,
                "/api/users/resend-verification", emailPerMinute,
                "/api/users/verify-email", verifyPerMinute), Clock.systemUTC());
    }

    AuthRateLimitFilter(Map<String, Integer> limits, Clock clock) {
        this.limits = limits;
        this.clock = clock;
    }

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        return !"POST".equals(request.getMethod()) || !limits.containsKey(request.getRequestURI());
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        String path = request.getRequestURI();
        long now = clock.millis();
        if (windows.size() > MAX_TRACKED_KEYS) {
            windows.entrySet().removeIf(e -> now - e.getValue().start >= WINDOW_MS);
        }

        String key = path + "|" + request.getRemoteAddr();
        Window window = windows.compute(key, (k, w) ->
                w == null || now - w.start >= WINDOW_MS ? new Window(now, 1) : new Window(w.start, w.count + 1));

        if (window.count > limits.get(path)) {
            long retryAfterSeconds = Math.max(1, (window.start + WINDOW_MS - now) / 1000);
            response.setStatus(429);
            response.setHeader("Retry-After", String.valueOf(retryAfterSeconds));
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write(
                    "{\"status\":429,\"message\":\"Too many attempts - try again in "
                            + retryAfterSeconds + " seconds\"}");
            return;
        }
        filterChain.doFilter(request, response);
    }

    private record Window(long start, int count) {
    }
}
