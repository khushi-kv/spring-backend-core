package com.example.basics.exception;

/**
 * Custom exception thrown when a requested database entity/resource is not found.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
