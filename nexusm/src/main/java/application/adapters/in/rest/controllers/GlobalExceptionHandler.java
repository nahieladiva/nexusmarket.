package application.adapters.in.rest.controllers;

import application.adapters.in.rest.responses.ErrorResponse;
import application.domain.exceptions.DomainException;
import application.domain.exceptions.InsufficientStockException;
import application.domain.exceptions.InvalidStatusTransitionException;
import application.domain.exceptions.OrderStateTransitionException;
import application.domain.exceptions.ResourceNotFoundException;
import application.domain.exceptions.UnauthorizedOperationException;
import application.domain.exceptions.UserAlreadyExistsException;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Traduce las excepciones del dominio y de validación a respuestas HTTP estandarizadas.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex,
                                                        HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler(UnauthorizedOperationException.class)
    public ResponseEntity<ErrorResponse> handleForbidden(UnauthorizedOperationException ex,
                                                         HttpServletRequest request) {
        return build(HttpStatus.FORBIDDEN, ex.getMessage(), request);
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ErrorResponse> handleMissingHeader(MissingRequestHeaderException ex,
                                                             HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST,
            "Falta la cabecera " + ex.getHeaderName() + " con el id del usuario que ejecuta la operación",
            request);
    }

    @ExceptionHandler({InsufficientStockException.class, OrderStateTransitionException.class,
        InvalidStatusTransitionException.class, UserAlreadyExistsException.class,
        IllegalStateException.class})
    public ResponseEntity<ErrorResponse> handleConflict(RuntimeException ex,
                                                        HttpServletRequest request) {
        return build(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    @ExceptionHandler({DomainException.class, IllegalArgumentException.class})
    public ResponseEntity<ErrorResponse> handleBadRequest(RuntimeException ex,
                                                          HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex,
                                                       HttpServletRequest request) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR,
            "Ocurrió un error interno en el servidor", request);
    }

    private ResponseEntity<ErrorResponse> build(HttpStatus status, String message,
                                                HttpServletRequest request) {
        ErrorResponse body = new ErrorResponse(
            LocalDateTime.now(), status.value(), status.getReasonPhrase(),
            message, request.getRequestURI());
        return ResponseEntity.status(status).body(body);
    }
}