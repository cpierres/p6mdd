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
     * Permet d'afficher un message synthétique lors de la validation des DTO (via  @Valid ou @Validated)
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

    @ExceptionHandler(MultipleResourceAlreadyExistException.class)
    public Mono<ResponseEntity<ValidationErrorResponse>> handleMultipleResourceAlreadyExistException(
            MultipleResourceAlreadyExistException ex) {

        // réponse structurée avec les erreurs des champs
        String msgGeneral = resolveErrorMessage(ex.getMessage(),
                "Un ou plusieurs conflits d'unicité existent");

        ValidationErrorResponse response = new ValidationErrorResponse(
                msgGeneral, // Message général
                ex.getFieldErrors() // Map des erreurs associées aux champs
        );

        return Mono.just(ResponseEntity
                .status(HttpStatus.CONFLICT) // Code 409 : Conflit
                .body(response));
    }

    /**
     * Résout le message d'erreur en vérifiant s'il est vide ou null.
     *
     * @param originalMessage Le message original.
     * @param defaultMessage Le message par défaut à utiliser si l'original est vide.
     * @return Le message d'erreur final.
     */
    private String resolveErrorMessage(String originalMessage, String defaultMessage) {
        return (originalMessage == null || originalMessage.isEmpty()) ? defaultMessage : originalMessage;
    }


    @ExceptionHandler(ResourceNotFoundException.class)
    public Mono<ResponseEntity<MessageResponse>> handleResourceNotFoundException(ResourceNotFoundException ex) {
        return Mono.just(ResponseEntity
                .status(HttpStatus.NOT_FOUND) // Code 404
                .body(new MessageResponse(ex.getMessage())));
    }
}