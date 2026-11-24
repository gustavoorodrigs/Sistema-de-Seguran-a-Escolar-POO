package com.escolasegura.core.exception;

import org.springframework.http.HttpStatus;

public class BusinessRuleException extends AppException {
    public BusinessRuleException(String message) {
        super(message, HttpStatus.CONFLICT);
    }
}
