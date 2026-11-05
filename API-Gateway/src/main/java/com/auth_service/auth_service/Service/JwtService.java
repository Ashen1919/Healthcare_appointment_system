package com.auth_service.auth_service.Service;

import com.auth_service.auth_service.Config.JwtConfig;
import com.auth_service.auth_service.Model.AuthModel;
import com.auth_service.auth_service.Model.Roles;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Date;
import java.util.UUID;

@AllArgsConstructor
@Service
public class JwtService {
    private final JwtConfig jwtConfig;

    public String generateAccessToken(AuthModel user) {
        return generateToken(user, jwtConfig.getAccessTokenExpiration(), "ACCESS");
    }

    public String generateRefreshToken(AuthModel user) {
        return generateToken(user, jwtConfig.getRefreshTokenExpiration(), "REFRESH");
    }

    private String generateToken(AuthModel user, long tokenExpiration, String tokenType) {
        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(String.valueOf(user.getId()))
                .claim("email", user.getEmail())
                .claim("role", user.getRole().name())
                .claim("token_type", tokenType)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + tokenExpiration))
                .signWith(jwtConfig.getSecretKey())
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            var claims = getClaims(token, jwtConfig.getSecret());
            return claims.getExpiration() != null && claims.getExpiration().after(new Date());
        } catch (JwtException ex) {
            return false;
        }
    }

    private static Claims getClaims(String token, String secret) {
        return Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)))
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public Claims getClaimsIgnoreExpiration(String token) {
        try {
            return getClaims(token, jwtConfig.getSecret());
        } catch (ExpiredJwtException ex) {
            return ex.getClaims();
        } catch (JwtException ex) {
            return null;
        }
    }

    public Duration getRemainingTtl(String token) {
        Claims claims = getClaimsIgnoreExpiration(token);
        if (claims == null || claims.getExpiration() == null) {
            return Duration.ZERO;
        }
        long diff = claims.getExpiration().getTime() - System.currentTimeMillis();
        return diff > 0 ? Duration.ofMillis(diff) : Duration.ZERO;
    }

    public String getTokenTypeFromToken(String token) {
        try {
            return getClaims(token, jwtConfig.getSecret()).get("token_type", String.class);
        } catch (JwtException e) {
            return null;
        }
    }

    public Long getUserIdFromToken(String token) {
        return Long.valueOf(getClaims(token, jwtConfig.getSecret()).getSubject());
    }

    public String getEmailFromToken(String token) {
        return getClaims(token, jwtConfig.getSecret()).get("email", String.class);
    }

    public Roles getRoleFromToken(String token) {
        return Roles.valueOf(getClaims(token, jwtConfig.getSecret()).get("role", String.class));
    }
}
