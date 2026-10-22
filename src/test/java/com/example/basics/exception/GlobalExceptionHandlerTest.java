package com.example.basics.exception;

import com.example.basics.model.Item;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.data.util.TypeInformation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;

import java.sql.SQLException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Covers the handlers that are hard to trigger through a controller.
 * The HTTP-facing handlers (400/401/403/404/405/409/415) are verified end to end in the controller tests.
 */
class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void dataIntegrityViolationReturns409WithoutLeakingDatabaseDetails() {
        DataIntegrityViolationException ex = new DataIntegrityViolationException("could not execute statement",
                new SQLException("Unique index violation: PUBLIC.ITEMS(SKU)"));

        ResponseEntity<ErrorResponse> response = handler.handleDataIntegrityViolation(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody().getMessage()).doesNotContain("ITEMS", "SKU");
    }

    @Test
    void unknownSortPropertyReturns400() {
        ResponseEntity<ErrorResponse> response = handler.handleInvalidProperty(unknownProperty());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getMessage()).isEqualTo("Invalid sort field 'doesNotExist'.");
    }

    @Test
    void wrappedUnknownSortPropertyReturns400() {
        InvalidDataAccessApiUsageException ex = new InvalidDataAccessApiUsageException("wrapped", unknownProperty());

        ResponseEntity<ErrorResponse> response = handler.handleInvalidDataAccessApiUsage(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void otherInvalidDataAccessApiUsageFallsBackTo500() {
        InvalidDataAccessApiUsageException ex = new InvalidDataAccessApiUsageException("something else");

        ResponseEntity<ErrorResponse> response = handler.handleInvalidDataAccessApiUsage(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    void missingRequestParameterReturns400() {
        ResponseEntity<ErrorResponse> response =
                handler.handleMissingParameter(new MissingServletRequestParameterException("page", "int"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getMessage()).contains("page");
    }

    @Test
    void unexpectedExceptionReturnsGeneric500WithoutInternalMessage() {
        ResponseEntity<ErrorResponse> response = handler.handleGeneralException(new IllegalStateException("db password=xyz"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().getStatus()).isEqualTo(500);
        assertThat(response.getBody().getTimestamp()).isNotNull();
        assertThat(response.getBody().getMessage()).doesNotContain("xyz");
        assertThat(response.getBody().getDetails()).doesNotContain("db password=xyz");
    }

    private static PropertyReferenceException unknownProperty() {
        return new PropertyReferenceException("doesNotExist", TypeInformation.of(Item.class), List.of());
    }
}
