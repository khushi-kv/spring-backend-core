package com.example.basics.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

/**
 * Global Exception Handler intercepting all controller-layer exceptions across the application.
 * Every error is returned in the same {@link ErrorResponse} JSON format with the correct HTTP status.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private static final String BAD_REQUEST = "Bad Request";

    // Handle Resource Not Found (404)
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(ResourceNotFoundException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, "Not Found", ex.getMessage(), List.of(ex.getMessage()));
    }

    // Handle Duplicate Resource, e.g. username/email/SKU already exists (409)
    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateResource(DuplicateResourceException ex) {
        return buildResponse(HttpStatus.CONFLICT, "Conflict", ex.getMessage(), List.of(ex.getMessage()));
    }

    // Handle database constraint violations, e.g. a unique column hit on update (409)
    // The raw database message is logged, never returned, as it can expose SQL and table details
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        logger.warn("Data integrity violation: {}", ex.getMostSpecificCause().getMessage());
        return buildResponse(HttpStatus.CONFLICT, "Conflict",
                "The request conflicts with existing data (for example, a duplicate value).",
                List.of("A record with the same unique value already exists."));
    }

    // Handle Bean Validation Errors (400)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(MethodArgumentNotValidException ex) {
        List<String> details = new ArrayList<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                details.add(error.getField() + ": " + error.getDefaultMessage())
        );
        return buildResponse(HttpStatus.BAD_REQUEST, "Bad Request - Validation Failed",
                "Input validation failed for one or more fields", details);
    }

    // Handle missing or malformed JSON request body (400)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleUnreadableMessage(HttpMessageNotReadableException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, BAD_REQUEST,
                "Request body is missing or is not valid JSON.",
                List.of("Check the JSON syntax and field types of the request body."));
    }

    // Handle wrong parameter type, e.g. /api/v1/items/abc where a number is expected (400)
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        Class<?> requiredType = ex.getRequiredType();
        String expectedType = requiredType != null ? requiredType.getSimpleName() : "a different type";
        String message = "Invalid value '" + ex.getValue() + "' for parameter '" + ex.getName() + "'.";
        return buildResponse(HttpStatus.BAD_REQUEST, BAD_REQUEST, message,
                List.of("Parameter '" + ex.getName() + "' must be of type " + expectedType + "."));
    }

    // Handle missing required query parameter (400)
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParameter(MissingServletRequestParameterException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, BAD_REQUEST, ex.getMessage(), List.of(ex.getMessage()));
    }

    // Handle unknown sort field, e.g. ?sortBy=doesNotExist (400)
    @ExceptionHandler(PropertyReferenceException.class)
    public ResponseEntity<ErrorResponse> handleInvalidProperty(PropertyReferenceException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, BAD_REQUEST,
                "Invalid sort field '" + ex.getPropertyName() + "'.", List.of(ex.getMessage()));
    }

    // Spring Data may wrap PropertyReferenceException when the query is executed
    @ExceptionHandler(InvalidDataAccessApiUsageException.class)
    public ResponseEntity<ErrorResponse> handleInvalidDataAccessApiUsage(InvalidDataAccessApiUsageException ex) {
        if (ex.getCause() instanceof PropertyReferenceException propertyEx) {
            return handleInvalidProperty(propertyEx);
        }
        return handleGeneralException(ex);
    }

    // Handle wrong login credentials (401)
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthentication(AuthenticationException ex) {
        return buildResponse(HttpStatus.UNAUTHORIZED, "Unauthorized",
                "Invalid username/email or password.",
                List.of("Authentication failed. Please check your credentials."));
    }

    // Handle Security Access Denied (403)
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex) {
        return buildResponse(HttpStatus.FORBIDDEN, "Forbidden - Access Denied",
                "You do not have the required permissions to perform this operation.",
                List.of(ex.getMessage()));
    }

    // Handle unknown URL (404)
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResourceFound(NoResourceFoundException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, "Not Found",
                "No endpoint found for '/" + ex.getResourcePath() + "'.",
                List.of("Check the URL. See /swagger-ui.html for available endpoints."));
    }

    // Handle wrong HTTP method, e.g. PATCH on an endpoint that only supports GET (405)
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        return buildResponse(HttpStatus.METHOD_NOT_ALLOWED, "Method Not Allowed", ex.getMessage(),
                List.of("Supported methods: " + ex.getSupportedHttpMethods()));
    }

    // Handle wrong Content-Type, e.g. sending text/plain instead of application/json (415)
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMediaTypeNotSupported(HttpMediaTypeNotSupportedException ex) {
        return buildResponse(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Unsupported Media Type", ex.getMessage(),
                List.of("Use Content-Type: application/json"));
    }

    // Fallback Handler for any unhandled unexpected exceptions (500)
    // The real error is logged for developers; users only see a generic message
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralException(Exception ex) {
        logger.error("Unhandled exception", ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error",
                "An unexpected error occurred. Please try again later.",
                List.of("An unexpected server error occurred."));
    }

    private ResponseEntity<ErrorResponse> buildResponse(HttpStatus status, String error, String message, List<String> details) {
        ErrorResponse errorResponse = new ErrorResponse(
                LocalDateTime.now(ZoneId.systemDefault()),
                status.value(),
                error,
                message,
                details
        );
        return new ResponseEntity<>(errorResponse, status);
    }
}
