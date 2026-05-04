package com.mint.habitus.presentation.activity;

import com.mint.habitus.application.activity.ActivityNotFoundException;
import com.mint.habitus.application.activity.ActivityValidationException;
import com.mint.habitus.application.activity.DuplicateActivityNameException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice(assignableTypes = ActivityController.class)
public class ActivityExceptionHandler {

    @ExceptionHandler(ActivityValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidation(ActivityValidationException exception) {
        return error(HttpStatus.BAD_REQUEST, "VALIDATION_FAILED", exception.getMessage());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleUnreadableJson(HttpMessageNotReadableException exception) {
        return error(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", "Request body is invalid");
    }

    @ExceptionHandler(ActivityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ActivityNotFoundException exception) {
        return error(HttpStatus.NOT_FOUND, "ACTIVITY_NOT_FOUND", exception.getMessage());
    }

    @ExceptionHandler(DuplicateActivityNameException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateName(DuplicateActivityNameException exception) {
        return error(HttpStatus.CONFLICT, "ACTIVITY_NAME_ALREADY_EXISTS", exception.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception exception) {
        log.error("Unexpected activity API error", exception);
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR", "Internal server error");
    }

    private ResponseEntity<ErrorResponse> error(HttpStatus status, String code, String message) {
        return ResponseEntity.status(status)
                .body(ErrorResponse.builder()
                        .code(code)
                        .message(message)
                        .timestamp(OffsetDateTime.now(ZoneOffset.UTC))
                        .build());
    }
}
