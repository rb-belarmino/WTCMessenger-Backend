package br.com.fiap.crm_backend.auth.service;

import br.com.fiap.crm_backend.auth.dto.LoginRequest;
import br.com.fiap.crm_backend.auth.dto.LoginResponse;
import br.com.fiap.crm_backend.exception.InvalidCredentialsException;
import br.com.fiap.crm_backend.security.CustomUserDetailsService;
import br.com.fiap.crm_backend.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthenticationManager authManager;

    @Mock
    private JwtTokenProvider jwtProvider;

    @Mock
    private CustomUserDetailsService userDetailsService;

    @InjectMocks
    private AuthService authService;

    private UserDetails userDetails;
    private LoginRequest validRequest;
    private LoginRequest invalidRequest;

    @BeforeEach
    void setUp() {
        userDetails = User.withUsername("operator@wtcmessenger.com")
                .password("encoded_password")
                .authorities("ROLE_OPERATOR")
                .build();

        validRequest = new LoginRequest("operator@wtcmessenger.com", "operator123");
        invalidRequest = new LoginRequest("wrong@wtcmessenger.com", "wrongpass");
    }

    @Test
    void shouldLoginSuccessfullyWhenCredentialsAreValid() {
        when(authManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(null); // authManager.authenticate returns Authentication, which we don't inspect
        when(userDetailsService.loadUserByUsername(validRequest.email())).thenReturn(userDetails);
        when(jwtProvider.generateAccessToken(userDetails)).thenReturn("mocked-access-token");
        when(jwtProvider.generateRefreshToken(userDetails)).thenReturn("mocked-refresh-token");

        LoginResponse response = authService.login(validRequest);

        assertNotNull(response);
        assertEquals("mocked-access-token", response.accessToken());
        assertEquals("mocked-refresh-token", response.refreshToken());
        assertEquals("Bearer", response.tokenType());
        assertEquals(900, response.expiresIn());
        assertEquals("ROLE_OPERATOR", response.role());

        verify(authManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userDetailsService, times(1)).loadUserByUsername(validRequest.email());
    }

    @Test
    void shouldThrowInvalidCredentialsExceptionWhenCredentialsAreInvalid() {
        when(authManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        InvalidCredentialsException exception = assertThrows(
                InvalidCredentialsException.class,
                () -> authService.login(invalidRequest)
        );

        assertEquals("Credenciais inválidas", exception.getMessage());
        verify(authManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verifyNoInteractions(userDetailsService, jwtProvider);
    }

    @Test
    void shouldRefreshTokenSuccessfullyWhenTokenIsValid() {
        String validRefreshToken = "valid-refresh-token";
        when(jwtProvider.extractUsername(validRefreshToken)).thenReturn("operator@wtcmessenger.com");
        when(userDetailsService.loadUserByUsername("operator@wtcmessenger.com")).thenReturn(userDetails);
        when(jwtProvider.isTokenValid(validRefreshToken, userDetails)).thenReturn(true);
        when(jwtProvider.generateAccessToken(userDetails)).thenReturn("new-access-token");

        LoginResponse response = authService.refreshToken(validRefreshToken);

        assertNotNull(response);
        assertEquals("new-access-token", response.accessToken());
        assertEquals(validRefreshToken, response.refreshToken());
        assertEquals("ROLE_OPERATOR", response.role());

        verify(jwtProvider, times(1)).extractUsername(validRefreshToken);
        verify(jwtProvider, times(1)).isTokenValid(validRefreshToken, userDetails);
        verify(jwtProvider, times(1)).generateAccessToken(userDetails);
    }

    @Test
    void shouldThrowInvalidCredentialsExceptionWhenRefreshTokenIsInvalid() {
        String invalidRefreshToken = "invalid-refresh-token";
        when(jwtProvider.extractUsername(invalidRefreshToken)).thenReturn("operator@wtcmessenger.com");
        when(userDetailsService.loadUserByUsername("operator@wtcmessenger.com")).thenReturn(userDetails);
        when(jwtProvider.isTokenValid(invalidRefreshToken, userDetails)).thenReturn(false);

        InvalidCredentialsException exception = assertThrows(
                InvalidCredentialsException.class,
                () -> authService.refreshToken(invalidRefreshToken)
        );

        assertEquals("Erro de autenticação", exception.getMessage()); // because it throws in the catch block of refreshToken
        verify(jwtProvider, times(1)).extractUsername(invalidRefreshToken);
        verify(jwtProvider, times(1)).isTokenValid(invalidRefreshToken, userDetails);
        verify(jwtProvider, never()).generateAccessToken(any(UserDetails.class));
    }
}
