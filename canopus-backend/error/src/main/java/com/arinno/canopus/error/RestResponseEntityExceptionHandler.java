package com.arinno.canopus.error;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.arinno.canopus.error.dto.ErrorMessage;


@ControllerAdvice
public class RestResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorMessage> responseStatusException(ResponseStatusException exception){
        return ErrorResponseFactory.of(exception.getStatusCode(), exception.getReason());
    }
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ErrorMessage> customException(CustomException exception){
        return ErrorResponseFactory.of(HttpStatus.INTERNAL_SERVER_ERROR, exception.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorMessage> exception(Exception exception){
        return ErrorResponseFactory.of(HttpStatus.INTERNAL_SERVER_ERROR, exception.getMessage());
    }

    @ExceptionHandler(TechnologyDataIntegrityException.class)
    public ResponseEntity<ErrorMessage> technologyDataIntegrityException(TechnologyDataIntegrityException exception){
        return ErrorResponseFactory.of(HttpStatus.CONFLICT, "No puedo eliminar esta tecnologia, tiene productos o usuarios asociados");
    }

    @ExceptionHandler(ProductDataIntegrityException.class)
    public ResponseEntity<ErrorMessage> productDataIntegrityException(ProductDataIntegrityException exception){
        return ErrorResponseFactory.of(HttpStatus.CONFLICT, "No puedo eliminar este producto, tiene proyectos asociados");
    }

    @ExceptionHandler(ProjectDataIntegrityException.class)
    public ResponseEntity<ErrorMessage> projectDataIntegrityException(ProjectDataIntegrityException exception){
        return ErrorResponseFactory.of(HttpStatus.CONFLICT, "No puedo eliminar este proyecto, tiene imputaciones asociadas");
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException exception,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        return ResponseEntity.status(status).body(ErrorResponseFactory.ofValidation(exception.getBindingResult()).getBody());
    }

}
