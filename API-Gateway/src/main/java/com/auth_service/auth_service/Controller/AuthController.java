package com.auth_service.auth_service.Controller;

import com.auth_service.auth_service.Config.JwtConfig;
import com.auth_service.auth_service.Dto.JwtResponseDto;
import com.auth_service.auth_service.Dto.LoginRequestDto;
import com.auth_service.auth_service.Dto.UserResponseDto;
import com.auth_service.auth_service.Repository.UserRepository;
import com.auth_service.auth_service.Service.JwtService;
import com.auth_service.auth_service.Service.TokenBlackListService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/auth")
@AllArgsConstructor
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final JwtConfig jwtConfig;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final TokenBlackListService tokenBlackListService;

    @PostMapping("/login")
    public ResponseEntity<JwtResponseDto> login(
            @Valid @RequestBody LoginRequestDto loginRequestDto,
            HttpServletResponse response
    ) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequestDto.getEmail(),
                        loginRequestDto.getPassword()
                )
        );

        var user = userRepository.findByEmail(loginRequestDto.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        var accessToken  = jwtService.generateAccessToken(user);
        var refreshToken = jwtService.generateRefreshToken(user);

        ResponseCookie cookie = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(true)
                .path("/api/v1/auth/refresh")
                .maxAge(Duration.ofMillis(jwtConfig.getRefreshTokenExpiration()))
                .sameSite("None")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        // Update last login timestamp for security audit trail
        user.setLast_login_at(LocalDateTime.now());
        userRepository.save(user);

        return ResponseEntity.ok(new JwtResponseDto(accessToken));
    }

    @PostMapping("/refresh")
    public ResponseEntity<JwtResponseDto> refreshToken(
            @CookieValue(value = "refreshToken", required = false) String refreshToken
    ) {
        if (refreshToken == null || !jwtService.validateToken(refreshToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Enforce token type: only REFRESH tokens are permitted on the refresh endpoint
        if (!"REFRESH".equals(jwtService.getTokenTypeFromToken(refreshToken))) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        if (tokenBlackListService.isBlacklisted(refreshToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        var userId = jwtService.getUserIdFromToken(refreshToken);
        var user = userRepository.findById(userId).orElse(null);
        if (user == null || !user.isEnabled()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        var accessToken = jwtService.generateAccessToken(user);
        return ResponseEntity.ok(new JwtResponseDto(accessToken));
    }

    @GetMapping("/currentUser")
    public ResponseEntity<UserResponseDto> getCurrentUser() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String userIdStr;
        if (authentication.getPrincipal() instanceof Jwt jwt) {
            userIdStr = jwt.getSubject();
        } else {
            userIdStr = authentication.getName();
        }

        Long userId;
        try {
            userId = Long.valueOf(userIdStr);
        } catch (NumberFormatException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        var user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }

        var userResponseDto = modelMapper.map(user, UserResponseDto.class);
        return ResponseEntity.ok(userResponseDto);
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(
            HttpServletRequest request,
            HttpServletResponse response,
            @CookieValue(value = "refreshToken", required = false) String refreshToken
    ) {
        // 1. Blacklist Access Token for its remaining lifespan (if provided)
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            String token = bearerToken.substring(7);
            Duration accessTtl = jwtService.getRemainingTtl(token);
            if (!accessTtl.isZero()) {
                tokenBlackListService.blacklistToken(token, accessTtl);
            }
        }

        // 2. Blacklist Refresh Token for its remaining lifespan (if provided)
        if (refreshToken != null) {
            Duration refreshTtl = jwtService.getRemainingTtl(refreshToken);
            if (!refreshTtl.isZero()) {
                tokenBlackListService.blacklistToken(refreshToken, refreshTtl);
            }
        }

        // 3. Delete refreshToken cookie with matching path
        ResponseCookie expiredCookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(true)
                .path("/api/v1/auth/refresh")
                .maxAge(0)
                .sameSite("None")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, expiredCookie.toString());

        return ResponseEntity.ok("Successfully logged out");
    }

    // Exception handlers
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Void> handleBadCredentialException() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<Void> handleUsernameNotFoundException() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<String> handleDisabledException(DisabledException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Account is disabled");
    }

    @ExceptionHandler(LockedException.class)
    public ResponseEntity<String> handleLockedException(LockedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Account is locked");
    }
}
