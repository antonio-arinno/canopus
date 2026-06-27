package com.arinno.canopus.error;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;


@ControllerAdvice
public class RestResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {
/*
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<String> customException(CustomException exception){
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(exception.getMessage());
    }
*/
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> exception(Exception exception){
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(exception.getMessage());
    }

    @ExceptionHandler(TechnologyDataIntegrityException.class)
    public ResponseEntity<String> technologyDataIntegrityException(TechnologyDataIntegrityException exception){
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("No puedo eliminar esta tecnologia, tiene productos o usuarios asociados");
    }

    @ExceptionHandler(ProductDataIntegrityException.class)
    public ResponseEntity<String> productDataIntegrityException(ProductDataIntegrityException exception){
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("No puedo eliminar este producto, tiene proyectos asociados");
    }

}
