package com.mdd.back.exception;

import com.mdd.back.models.ErrorDetails;
import com.mdd.back.models.FieldErrorDetail;
import com.mdd.back.models.Severity;
import com.mdd.back.models.ValidationErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Permet d'afficher un message synthétique lors de la validation des DTO (via @Valid ou @Validated)
     */
    @ExceptionHandler(WebExchangeBindException.class)
    public Mono<ResponseEntity<ValidationErrorResponse>> handleValidationException(WebExchangeBindException ex) {
        List<FieldErrorDetail> fieldErrorDetails = convertFieldErrors(ex.getBindingResult().getFieldErrors());
        ValidationErrorResponse errorResponse = new ValidationErrorResponse(
                "Les données d'entrée ne sont pas valides.",
                fieldErrorDetails
        );
        return Mono.just(ResponseEntity.badRequest().body(errorResponse));
    }

    private List<FieldErrorDetail> convertFieldErrors(List<FieldError> fieldErrors) {
        return fieldErrors.stream()
                .map(error -> new FieldErrorDetail(
                        error.getField(),
                        error.getDefaultMessage(),
                        Severity.ERROR // Sévérité par défaut si non spécifiée
                ))
                .toList();
    }

    @ExceptionHandler(ResourceAlreadyExistException.class)
    public Mono<ResponseEntity<ErrorDetails>> handleResourceAlreadyExistException(ResourceAlreadyExistException ex) {
        ErrorDetails response = new ErrorDetails(
                ex.getMessage(),
                Severity.WARNING, // Sévérité attribuée
                null // Pas d'erreurs sur champs spécifiques donc null
        );
        return Mono.just(ResponseEntity
                .status(HttpStatus.CONFLICT) // Code 409
                .body(response));
    }

    @ExceptionHandler(MultipleResourceAlreadyExistException.class)
    public Mono<ResponseEntity<ValidationErrorResponse>> handleMultipleResourceAlreadyExistException(
            MultipleResourceAlreadyExistException ex) {

        // réponse structurée avec les erreurs des champs
        String msgGeneral = resolveErrorMessage(ex.getMessage(),
                "Un ou plusieurs conflits d'unicité existent");

        ValidationErrorResponse response = new ValidationErrorResponse();
        response.setMessage(msgGeneral);
        response.setFieldErrors(ex.getFieldErrors());
        response.setSeverity(Severity.WARNING); // Définir explicitement WARNING

        return Mono.just(ResponseEntity
                .status(HttpStatus.CONFLICT) // Code 409 : Conflit
                .body(response));
    }

    /**
     * Résout le message d'erreur en vérifiant s'il est vide ou null.
     *
     * @param originalMessage Le message original.
     * @param defaultMessage  Le message par défaut à utiliser si l'original est vide.
     * @return Le message d'erreur final.
     */
    private String resolveErrorMessage(String originalMessage, String defaultMessage) {
        return (originalMessage == null || originalMessage.isEmpty()) ? defaultMessage : originalMessage;
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public Mono<ResponseEntity<ErrorDetails>> handleResourceNotFoundException(ResourceNotFoundException ex) {
        ErrorDetails response = new ErrorDetails(
                ex.getMessage(),
                Severity.INFO, // Sévérité de type "info" attribuée à cette erreur
                null
        );
        return Mono.just(ResponseEntity
                .status(HttpStatus.NOT_FOUND) // Code 404
                .body(response));
    }

    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<ErrorDetails>> handleGeneralException(Exception ex) {
        //Par sécurité, on ne montre pas le détail de l'erreur technique à l'utilisateur
        // on se contente ici de tracer l'erreur avec un log
        ErrorDetails response = new ErrorDetails(
                "Une erreur interne est survenue. Veuillez réessayer ultérieurement.",
                Severity.ERROR,
                null
        );
        log.error("Une erreur interne est survenue : ", ex);
        return Mono.just(ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR) // Code 500
                .body(response));
    }

}