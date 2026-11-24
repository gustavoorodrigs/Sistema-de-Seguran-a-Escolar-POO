package com.escolasegura.modules.auth.service;

import com.escolasegura.core.exception.InactiveUserException;
import com.escolasegura.core.exception.InvalidCredentialsException;
import com.escolasegura.core.security.JwtService;
import com.escolasegura.modules.auth.dto.LoginRequest;
import com.escolasegura.modules.auth.dto.LoginResponse;
import com.escolasegura.modules.auth.model.Role;
import com.escolasegura.modules.auth.model.User;
import com.escolasegura.modules.auth.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private JwtService jwtService;
    private AuthService authService;

    private User activeUser;
    private User inactiveUser;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService("c2Nob29sLXNlY3VyaXR5LXN5c3RlbS1zdXBlci1zZWNyZXQta2V5LTIwMjY=", 86400000L);
        authService = new AuthService(userRepository, passwordEncoder, jwtService);

        activeUser = new User("Carlos Silva", "carlos@escola.com", "encodedHash", Role.SUPERVISOR, true);
        activeUser.setId(1L);

        inactiveUser = new User("Inativo", "inativo@escola.com", "encodedHash", Role.RESPONSAVEL, false);
        inactiveUser.setId(2L);
    }

    @Test
    @DisplayName("Deve autenticar com sucesso para credenciais válidas e usuário ativo")
    void shouldLoginSuccessfully() {
        LoginRequest request = new LoginRequest("carlos@escola.com", "Senha@123");

        when(userRepository.findByEmail("carlos@escola.com")).thenReturn(Optional.of(activeUser));
        when(passwordEncoder.matches("Senha@123", "encodedHash")).thenReturn(true);

        LoginResponse response = authService.login(request);

        assertThat(response).isNotNull();
        assertThat(response.token()).isNotBlank();
        assertThat(jwtService.isTokenValid(response.token())).isTrue();
        assertThat(response.user().email()).isEqualTo("carlos@escola.com");
        assertThat(response.user().role()).isEqualTo(Role.SUPERVISOR);
        assertThat(response.user().roleDescription()).isEqualTo("Supervisor");
    }

    @Test
    @DisplayName("Deve falhar ao tentar login com senha incorreta")
    void shouldFailWhenPasswordDoesNotMatch() {
        LoginRequest request = new LoginRequest("carlos@escola.com", "SenhaErrada");

        when(userRepository.findByEmail("carlos@escola.com")).thenReturn(Optional.of(activeUser));
        when(passwordEncoder.matches("SenhaErrada", "encodedHash")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    @DisplayName("Deve falhar ao tentar login com e-mail não existente")
    void shouldFailWhenEmailNotFound() {
        LoginRequest request = new LoginRequest("naoexiste@escola.com", "Senha@123");

        when(userRepository.findByEmail("naoexiste@escola.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    @DisplayName("Deve barrar usuário com cadastro inativo (RF001)")
    void shouldBlockInactiveUser() {
        LoginRequest request = new LoginRequest("inativo@escola.com", "Senha@123");

        when(userRepository.findByEmail("inativo@escola.com")).thenReturn(Optional.of(inactiveUser));
        when(passwordEncoder.matches("Senha@123", "encodedHash")).thenReturn(true);

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(InactiveUserException.class);
    }
}
