package br.com.fiap.crm_backend.auth.service;

import br.com.fiap.crm_backend.auth.dto.LoginRequest;
import br.com.fiap.crm_backend.auth.dto.LoginResponse;
import br.com.fiap.crm_backend.exception.InvalidCredentialsException;
import br.com.fiap.crm_backend.security.CustomUserDetailsService;
import br.com.fiap.crm_backend.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final AuthenticationManager authManager;
    private final JwtTokenProvider jwtProvider;
    private final CustomUserDetailsService userDetailsService;

    public LoginResponse login(LoginRequest request) {
        try {
            authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.email(),
                            request.password()
                    )
            );
        } catch (BadCredentialsException ex) {
            throw new InvalidCredentialsException();
        }

        UserDetails userDetails = userDetailsService
                .loadUserByUsername(request.email());

        String accessToken = jwtProvider.generateAccessToken(userDetails);
        String refreshToken = jwtProvider.generateRefreshToken(userDetails);

        String role = userDetails.getAuthorities()
                .iterator().next().getAuthority();

        return new LoginResponse(
                accessToken,
                refreshToken,
                "Bearer",
                900,
                role
        );
    }

    public LoginResponse refreshToken(String refreshToken) {
        try {
            String username = jwtProvider.extractUsername(refreshToken);
            UserDetails userDetails = userDetailsService
                    .loadUserByUsername(username);

            if (!jwtProvider.isTokenValid(refreshToken, userDetails)) {
                throw new InvalidCredentialsException();
            }

            String newAccessToken = jwtProvider
                    .generateAccessToken(userDetails);

            String role = userDetails.getAuthorities()
                    .iterator().next().getAuthority();

            return new LoginResponse(
                    newAccessToken,
                    refreshToken,
                    "Bearer",
                    900,
                    role
            );
        } catch (Exception ex) {
            throw new InvalidCredentialsException();
        }
    }
}