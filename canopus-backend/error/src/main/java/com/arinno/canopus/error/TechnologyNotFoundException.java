package com.arinno.canopus.error;

public class TechnologyNotFoundException extends RuntimeException {
    public TechnologyNotFoundException(String message) {
        super(message);
    }
}
