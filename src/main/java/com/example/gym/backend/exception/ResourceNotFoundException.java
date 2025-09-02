package com.example.gym.backend.exception;


import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when a requested resource is not found
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {

    /**
     * Constructor with message
     * @param message the error message
     */
    public ResourceNotFoundException(String message) {
        super(message);
    }

    /**
     * Constructor with message and cause
     * @param message the error message
     * @param cause the cause of the exception
     */
    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructor with resource type and ID
     * @param resourceType the type of resource (e.g., "Member", "Trainer")
     * @param fieldName the field name used for search
     * @param fieldValue the field value that was not found
     */
    public ResourceNotFoundException(String resourceType, String fieldName, Object fieldValue) {
        super(String.format("%s not found with %s : '%s'", resourceType, fieldName, fieldValue));
    }
}
