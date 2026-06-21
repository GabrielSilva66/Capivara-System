package com.example.capivacore.modules.web.handler;


import com.example.capivacore.modules.web.dto.ApiError;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<String> handleConstraintViolation(ConstraintViolationException ex) {
        return ResponseEntity
                .badRequest()
                .body("Validation error: " + ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> genericException(Exception ex) {
        return createResponseApiError(HttpStatus.INTERNAL_SERVER_ERROR, List.of(ex.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> illegalArgumentException(IllegalArgumentException ex) {
        return createResponseApiError(HttpStatus.BAD_REQUEST, List.of(ex.getMessage()));
    }

    private ResponseEntity<ApiError> createResponseApiError(HttpStatus status, List<String> errors) {
        ApiError apiError = ApiError
                .builder()
                .timestamp(LocalDateTime.now())
                .code(status.value())
                .status(status.name())
                .errors(errors)
                .build();
        return new ResponseEntity<>(apiError, status);
    }
}
