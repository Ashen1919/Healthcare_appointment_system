package com.auth_service.auth_service.Service;

import com.auth_service.auth_service.Config.JwtConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class TokenBlackListService {
    private final RedisTemplate<String, String> redisTemplate;
    private final JwtConfig jwtConfig;

    /**
     * Blacklists a token for its specific remaining validity duration.
     */
    public void blacklistToken(String token, Duration ttl) {
        if (token != null && !token.isBlank() && ttl != null && !ttl.isZero() && !ttl.isNegative()) {
            redisTemplate.opsForValue().set(
                    "blacklist:" + token,
                    "true",
                    ttl
            );
        }
    }

    /**
     * Fallback blacklisting using default access token expiration duration.
     */
    public void blacklistToken(String token) {
        blacklistToken(token, Duration.ofMillis(jwtConfig.getAccessTokenExpiration()));
    }

    public boolean isBlacklisted(String token) {
        if (token == null || token.isBlank()) {
            return false;
        }
        return Boolean.TRUE.equals(redisTemplate.hasKey("blacklist:" + token));
    }
}
