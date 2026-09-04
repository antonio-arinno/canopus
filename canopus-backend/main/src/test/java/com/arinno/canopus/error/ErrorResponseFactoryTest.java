package com.arinno.canopus.error;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import com.arinno.canopus.error.dto.ErrorMessage;

class ErrorResponseFactoryTest {

    @Test
    void ofBuildsConsistentErrorBody() {
        ResponseEntity<ErrorMessage> response = ErrorResponseFactory.of(HttpStatus.BAD_REQUEST, "Solicitud inválida.");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        ErrorMessage body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getStatus()).isEqualTo(400);
        assertThat(body.getError()).isEqualTo("Bad Request");
        assertThat(body.getMessage()).isEqualTo("Solicitud inválida.");
    }

    @Test
    void ofValidationIncludesFirstFieldMessageAndFieldErrorsMap() {
        BindingResult result = mock(BindingResult.class);
        FieldError fieldError = new FieldError("request", "name", "no puede estar vacío");
        when(result.getFieldErrors()).thenReturn(List.of(fieldError));

        ResponseEntity<ErrorMessage> response = ErrorResponseFactory.ofValidation(result);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        ErrorMessage body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getMessage()).isEqualTo("El campo name no puede estar vacío");
        assertThat(body.getFieldErrors()).containsEntry("name", "El campo name no puede estar vacío");
    }
}
