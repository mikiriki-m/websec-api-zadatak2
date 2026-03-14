package com.example.websecurity.facade;

import com.example.websecurity.api.dto.AuthenticationRequest;
import com.example.websecurity.api.dto.AuthenticationResponse;
import com.example.websecurity.security.JwtService;
import com.example.websecurity.service.UserService;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationFacade {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserService userService;

    @Transactional(noRollbackFor = {org.springframework.security.core.AuthenticationException.class})
    public AuthenticationResponse authenticate(@NotNull AuthenticationRequest request) {
        var user = userService.getUserByEmail(request.getEmail());

        if (user.getLockoutExpiry() != null && user.getLockoutExpiry().isAfter(java.time.LocalDateTime.now())) {
            log.warn("Login denied: User {} is currently locked out.", user.getEmail());
            // Throw 423 Locked
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.LOCKED, "Account is locked. Try again later."
            );
        }

        try {
            log.info("Authentication Facade: Attempting login for user: {}", request.getEmail());

            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );

            user.setFailedLoginAttempts(0);
            user.setLockoutExpiry(null);
            userService.save(user);

            var accessToken = jwtService.generateAccessToken(user);
            return AuthenticationResponse.builder()
                    .accessToken(accessToken)
                    .build();

        } catch (org.springframework.security.core.AuthenticationException e) {
            int newAttempts = user.getFailedLoginAttempts() + 1;
            user.setFailedLoginAttempts(newAttempts);
            log.info("Failed login attempt #{} for user {}", newAttempts, user.getEmail());

            if (newAttempts >= 3) {
                user.setLockoutExpiry(java.time.LocalDateTime.now().plusMinutes(5));
                log.warn("User {} has reached 3 failures and is now locked out.", user.getEmail());
            }

            userService.save(user);

            throw e;
        }
    }
}
