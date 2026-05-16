
package br.com.fiap.crm_backend.auth.controller;

import br.com.fiap.crm_backend.auth.dto.LoginRequest;
import br.com.fiap.crm_backend.auth.dto.LoginResponse;
import br.com.fiap.crm_backend.auth.dto.RefreshRequest;
import br.com.fiap.crm_backend.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refresh(
            @Valid @RequestBody RefreshRequest request) {
        return ResponseEntity.ok(
                authService.refreshToken(request.refreshToken()));
    }
}