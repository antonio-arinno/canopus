package com.arinno.canopus.error;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;

import com.arinno.canopus.error.dto.ErrorMessage;

// Single place to build the consistent error response shape used across the API.
public final class ErrorResponseFactory {

    private ErrorResponseFactory() {
    }

    public static ResponseEntity<ErrorMessage> of(HttpStatusCode statusCode, String message) {
        HttpStatus status = HttpStatus.valueOf(statusCode.value());
        ErrorMessage body = ErrorMessage.builder()
                .timestamp(Instant.now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(message)
                .build();
        return ResponseEntity.status(status).body(body);
    }

    public static ResponseEntity<ErrorMessage> ofValidation(BindingResult result) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        result.getFieldErrors().forEach(error ->
                fieldErrors.put(error.getField(), "El campo " + error.getField() + " " + error.getDefaultMessage()));

        String message = fieldErrors.values().stream().findFirst().orElse("Los datos enviados no son válidos.");

        ErrorMessage body = ErrorMessage.builder()
                .timestamp(Instant.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .message(message)
                .fieldErrors(fieldErrors)
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }
}
