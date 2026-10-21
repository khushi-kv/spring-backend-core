package com.example.basics.exception;

/**
 * Custom exception thrown when creating a resource that conflicts with an existing one
 * (e.g. duplicate username, email, category name or item SKU).
 */
public class DuplicateResourceException extends RuntimeException {

    public DuplicateResourceException(String message) {
        super(message);
    }
}
