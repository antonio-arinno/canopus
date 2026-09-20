package com.arinno.canopus.error;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.RestControllerAdvice; // Cambiado por @ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;

import com.arinno.canopus.error.dto.ErrorMessage;

@RestControllerAdvice
public class RestResponseEntityExceptionHandler { // Eliminado el 'extends ResponseEntityExceptionHandler'

    // 1. Validaciones de formularios (@Valid) - Limpio y sin warnings de tipos
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleMethodArgumentNotValid(MethodArgumentNotValidException exception) {
        return ErrorResponseFactory.ofValidation(exception.getBindingResult());
    }

    // 2. Excepciones nativas de Spring con estados HTTP específicos
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorMessage> responseStatusException(ResponseStatusException exception){
        return ErrorResponseFactory.of(exception.getStatusCode(), exception.getReason());
    }

    // 3. Excepciones de lógica de negocio del proyecto
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ErrorMessage> customException(CustomException exception){
        return ErrorResponseFactory.of(HttpStatus.INTERNAL_SERVER_ERROR, exception.getMessage());
    }

    // 4. Excepciones de Integridad de Datos (Data Integrity) para Base de Datos
    // CAPTURA INTELIGENTE CENTRALIZADA: Mantiene los mensajes personalizados analizando el error de MySQL
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorMessage> handleDataIntegrityViolation(DataIntegrityViolationException exception) {
        String rootMsg = exception.getRootCause() != null ? exception.getRootCause().getMessage().toLowerCase() : "";
        String customMessage;

        // Evaluamos de forma inteligente qué entidad está causando la colisión en la Base de Datos
        if (rootMsg.contains("product")) {
            customMessage = "No puedo eliminar este producto, tiene proyectos asociados.";
        } else if (rootMsg.contains("technology") || rootMsg.contains("tecnologia")) {
            customMessage = "No puedo eliminar esta tecnología, tiene productos o usuarios asociados.";
        } else if (rootMsg.contains("project") || rootMsg.contains("proyecto")) {
            customMessage = "No puedo eliminar este proyecto, tiene imputaciones asociadas.";
        } else {
            customMessage = "No se puede eliminar o modificar este elemento porque tiene dependencias o registros asociados en el sistema.";
        }

        return ErrorResponseFactory.of(HttpStatus.CONFLICT, customMessage);
    }

    // Añade estos dos nuevos métodos a tu RestResponseEntityExceptionHandler
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorMessage> userNotFoundException(UserNotFoundException exception) {
        return ErrorResponseFactory.of(HttpStatus.NOT_FOUND, exception.getMessage());
    }

    @ExceptionHandler(InvalidPasswordException.class)
    public ResponseEntity<ErrorMessage> invalidPasswordException(InvalidPasswordException exception) {
        return ErrorResponseFactory.of(HttpStatus.BAD_REQUEST, exception.getMessage());
    }

    // 5. Captura genérica (Cualquier error inesperado no controlado previamente)
    // NOTA: Se deja al final para que Spring evalúe primero las excepciones específicas de arriba
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorMessage> exception(Exception exception){
        return ErrorResponseFactory.of(HttpStatus.INTERNAL_SERVER_ERROR, exception.getMessage());
    }
}
