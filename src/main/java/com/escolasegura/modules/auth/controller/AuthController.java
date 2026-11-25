package com.escolasegura.modules.auth.controller;

import com.escolasegura.modules.auth.dto.ChangePasswordRequest;
import com.escolasegura.modules.auth.dto.LoginRequest;
import com.escolasegura.modules.auth.dto.LoginResponse;
import com.escolasegura.modules.auth.dto.UserResponse;
import com.escolasegura.modules.auth.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> me(@AuthenticationPrincipal String email) {
        UserResponse response = authService.getProfile(email);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/password")
    public ResponseEntity> changePassword(
            @AuthenticationPrincipal String email,
            @Valid @RequestBody ChangePasswordRequest request
    ) {
        authService.changePassword(email, request);
        return ResponseEntity.ok(Map.of("message", "Senha alterada com sucesso."));
    }
}