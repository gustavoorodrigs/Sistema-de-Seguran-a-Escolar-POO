package com.escolasegura.modules.auth.service;

import com.escolasegura.core.exception.InactiveUserException;
import com.escolasegura.core.exception.InvalidCredentialsException;
import com.escolasegura.core.security.JwtService;
import com.escolasegura.modules.auth.dto.LoginRequest;
import com.escolasegura.modules.auth.dto.LoginResponse;
import com.escolasegura.modules.auth.dto.UserResponse;
import com.escolasegura.modules.auth.model.User;
import com.escolasegura.modules.auth.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email().trim().toLowerCase())
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        if (!user.isActive()) {
            throw new InactiveUserException();
        }

        String token = jwtService.generateToken(user);
        return LoginResponse.of(token, jwtService.getExpirationMs(), UserResponse.fromEntity(user));
    }

    @Transactional(readOnly = true)
    public UserResponse getProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(InvalidCredentialsException::new);
        return UserResponse.fromEntity(user);
    }
}
