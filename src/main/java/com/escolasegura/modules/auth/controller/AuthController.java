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

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> me(@AuthenticationPrincipal String email) {
        return ResponseEntity.ok(authService.getProfile(email));
    }

    @PatchMapping("/password")
    public ResponseEntity<UserResponse> changePassword(@AuthenticationPrincipal String email, @Valid @RequestBody ChangePasswordRequest request) {
        return ResponseEntity.ok(authService.changePassword(email, request));
    }
}
