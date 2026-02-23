package com.inditex.pricing.adapter.in.web;

import com.inditex.pricing.domain.exception.PriorityConflictException;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

/**
 * Centralized exception handler for REST controllers.
 * Converts exceptions into structured error responses with appropriate HTTP status codes.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @Schema(description = "Respuesta de error estandar de la API")
    public record ErrorResponse(
            @Schema(description = "Codigo de estado HTTP", example = "400") int status,
            @Schema(description = "Descripcion del error", example = "Bad Request") String error,
            @Schema(description = "Mensaje descriptivo del error", example = "Missing required parameter: applicationDate") String message,
            @Schema(description = "Momento en que se produjo el error", example = "2020-06-14T16:00:00") LocalDateTime timestamp
    ) {}

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParameter(MissingServletRequestParameterException ex) {
        var error = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                "Missing required parameter: " + ex.getParameterName(),
                LocalDateTime.now()
        );
        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        var error = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                "Invalid value for parameter '" + ex.getName() + "': " + ex.getValue(),
                LocalDateTime.now()
        );
        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException ex) {
        String message = ex.getConstraintViolations().stream()
                .map(cv -> {
                    String path = cv.getPropertyPath().toString();
                    String paramName = path.contains(".") ? path.substring(path.lastIndexOf('.') + 1) : path;
                    return "Invalid value for parameter '" + paramName + "': " + cv.getMessage();
                })
                .collect(Collectors.joining(", "));
        var error = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                message,
                LocalDateTime.now()
        );
        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(PriorityConflictException.class)
    public ResponseEntity<ErrorResponse> handlePriorityConflict(PriorityConflictException ex) {
        log.error("Conflicto de prioridad detectado: {}", ex.getMessage());
        var error = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Data Integrity Error",
                ex.getMessage(),
                LocalDateTime.now()
        );
        return ResponseEntity.internalServerError().body(error);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Void> handleNoResourceFound(NoResourceFoundException ex) {
        return ResponseEntity.notFound().build();
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        log.error("Excepcion no controlada en el procesamiento de la peticion", ex);
        var error = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error",
                "An unexpected error occurred",
                LocalDateTime.now()
        );
        return ResponseEntity.internalServerError().body(error);
    }
}
