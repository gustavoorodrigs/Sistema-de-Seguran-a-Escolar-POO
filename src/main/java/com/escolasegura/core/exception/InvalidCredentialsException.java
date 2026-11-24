package com.escolasegura.core.exception;

import org.springframework.http.HttpStatus;

public class InvalidCredentialsException extends AppException {
    public InvalidCredentialsException() {
        super("Credenciais inválidas. Verifique seu e-mail e senha.", HttpStatus.UNAUTHORIZED);
    }
}
