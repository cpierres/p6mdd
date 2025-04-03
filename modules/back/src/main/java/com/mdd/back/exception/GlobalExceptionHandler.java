package com.mdd.back.exception;

import com.mdd.back.models.MessageResponse;
import com.mdd.back.models.ValidationErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Permet d'afficher un message synthétique lors de la validation des DTO
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Mono<ResponseEntity<ValidationErrorResponse>> handleValidationException(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult()
                .getFieldErrors()
                .forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));

        ValidationErrorResponse response = new ValidationErrorResponse(
                "Les données d'entrée ne sont pas valides.",
                errors
        );

        return Mono.just(ResponseEntity.badRequest().body(response));
    }

    @ExceptionHandler(ResourceAlreadyExistException.class)
    public Mono<ResponseEntity<MessageResponse>> handleResourceAlreadyExistException(ResourceAlreadyExistException ex) {
        return Mono.just(ResponseEntity
                .status(HttpStatus.CONFLICT) // Code 409
                .body(new MessageResponse(ex.getMessage())));
    }

}