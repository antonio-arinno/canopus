package com.arinno.canopus.error;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

import com.arinno.canopus.error.dto.ErrorMessage;

class RestResponseEntityExceptionHandlerTest {

    private final RestResponseEntityExceptionHandler handler = new RestResponseEntityExceptionHandler();

    @Test
    void responseStatusExceptionUsesReasonAsMessageAndMatchingStatus() {
        ResponseStatusException exception = new ResponseStatusException(HttpStatus.NOT_FOUND, "Recurso no encontrado.");

        ResponseEntity<ErrorMessage> response = handler.responseStatusException(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        ErrorMessage body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getStatus()).isEqualTo(404);
        assertThat(body.getError()).isEqualTo("Not Found");
        assertThat(body.getMessage()).isEqualTo("Recurso no encontrado.");
        assertThat(body.getTimestamp()).isNotNull();
    }

    @Test
    void customExceptionMapsToInternalServerError() {
        CustomException exception = new CustomException("Algo salió mal.");

        ResponseEntity<ErrorMessage> response = handler.customException(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().getMessage()).isEqualTo("Algo salió mal.");
    }


    @Test
    void dataIntegrityExceptionsReturnConflictWithFixedMessage() {
        // Simulamos una excepción nativa de Spring que contenga la palabra "product" en su causa raíz
        DataIntegrityViolationException simulatedException = new DataIntegrityViolationException(
                "Fallo de integridad", 
                new java.sql.SQLIntegrityConstraintViolationException("Cannot delete or update a parent row: a foreign key constraint fails (`db_canopus`.`projects`, CONSTRAINT `fk_project_product` FOREIGN KEY (`product_id`))")
        );

        ResponseEntity<ErrorMessage> response = handler.handleDataIntegrityViolation(simulatedException);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody().getMessage()).contains("tiene proyectos asociados");
    }



}
