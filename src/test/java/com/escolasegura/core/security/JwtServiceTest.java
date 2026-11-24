package com.escolasegura.core.security;

import com.escolasegura.modules.auth.model.Role;
import com.escolasegura.modules.auth.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private JwtService jwtService;
    private final String secret = "c2Nob29sLXNlY3VyaXR5LXN5c3RlbS1zdXBlci1zZWNyZXQta2V5LTIwMjY=";
    private final long expirationMs = 3600000;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(secret, expirationMs);
    }

    @Test
    @DisplayName("Deve gerar token JWT válido com claims de usuário e perfil")
    void shouldGenerateValidToken() {
        User user = new User("Ana Paula", "ana@escola.com", "hash", Role.COORDENADOR, true);
        user.setId(10L);

        String token = jwtService.generateToken(user);

        assertThat(token).isNotBlank();
        assertThat(jwtService.isTokenValid(token)).isTrue();
        assertThat(jwtService.extractEmail(token)).isEqualTo("ana@escola.com");
        assertThat(jwtService.extractRole(token)).isEqualTo("COORDENADOR");
        assertThat(jwtService.extractUserId(token)).isEqualTo(10L);
    }

    @Test
    @DisplayName("Deve retornar false para token adulterado ou inválido")
    void shouldRejectInvalidToken() {
        assertThat(jwtService.isTokenValid("invalid.jwt.token")).isFalse();
        assertThat(jwtService.isTokenValid("")).isFalse();
    }
}
