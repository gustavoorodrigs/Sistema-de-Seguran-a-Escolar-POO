package com.escolasegura.core.exception;

import org.springframework.http.HttpStatus;

public class InactiveUserException extends AppException {
    public InactiveUserException() {
        super("Acesso negado. Usuário inativo no sistema.", HttpStatus.FORBIDDEN);
    }
}
