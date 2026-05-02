package com.mint.habitus.presentation.activityhistory;

import com.mint.habitus.application.activityhistory.exception.ActivityHistoryNotFoundException;
import com.mint.habitus.application.activityhistory.exception.ActivityHistoryValidationException;
import com.mint.habitus.presentation.activity.ErrorResponse;
import java.time.LocalDateTime;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice(assignableTypes = ActivityHistoryController.class)
public class ActivityHistoryExceptionHandler {

    @ExceptionHandler(ActivityHistoryNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ActivityHistoryNotFoundException exception) {
        return error(HttpStatus.NOT_FOUND, "ACTIVITY_HISTORY_NOT_FOUND", exception.getMessage());
    }

    @ExceptionHandler(ActivityHistoryValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidation(ActivityHistoryValidationException exception) {
        return error(HttpStatus.BAD_REQUEST, "VALIDATION_FAILED", exception.getMessage());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleUnreadableJson(HttpMessageNotReadableException exception) {
        return error(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", "Request body is invalid");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception exception) {
        log.error("Unexpected activity history API error", exception);
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR", "Internal server error");
    }

    private ResponseEntity<ErrorResponse> error(HttpStatus status, String code, String message) {
        return ResponseEntity.status(status)
                .body(ErrorResponse.builder()
                        .code(code)
                        .message(message)
                        .timestamp(LocalDateTime.now())
                        .build());
    }
}
