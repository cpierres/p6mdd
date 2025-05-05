package com.mdd.back.exception;

import com.mdd.back.models.ApiResult;
import com.mdd.back.models.ResponseDetails;
import com.mdd.back.models.FieldInfoDetails;
import com.mdd.back.models.Severity;
import com.mdd.back.utils.context.RequestIdContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Permet d'afficher un message synthétique lors de la validation des DTO (via @Valid ou @Validated)
     */
    @ExceptionHandler(WebExchangeBindException.class)
    public Mono<ResponseEntity<ApiResult<ResponseDetails>>> handleValidationException(WebExchangeBindException ex, ServerWebExchange exchange) {
        // Récupérer l'ID de requête depuis les attributs d'échange
        String requestId = (String) exchange.getAttributes().get(RequestIdContext.REQUEST_ID_KEY);

        List<FieldInfoDetails> fieldErrorDetails = convertFieldErrors(ex.getBindingResult().getFieldErrors());
        ResponseDetails responseDetails = new ResponseDetails(
                "Les données d'entrée ne sont pas valides.",
                fieldErrorDetails
        );

        ApiResult<ResponseDetails> apiResult = new ApiResult<>(
                responseDetails,
            "Erreur de validation",
            HttpStatus.BAD_REQUEST.value(),
            requestId
        );

        return Mono.just(ResponseEntity.badRequest().body(apiResult));
    }

    private List<FieldInfoDetails> convertFieldErrors(List<FieldError> fieldErrors) {
        return fieldErrors.stream()
                .map(error -> new FieldInfoDetails(
                        error.getField(),
                        error.getDefaultMessage(),
                        Severity.ERROR // Sévérité par défaut si non spécifiée
                ))
                .toList();
    }

    @ExceptionHandler(ResourceAlreadyExistException.class)
    public Mono<ResponseEntity<ApiResult<ResponseDetails>>> handleResourceAlreadyExistException(ResourceAlreadyExistException ex, ServerWebExchange exchange) {
        // Récupérer l'ID de requête depuis les attributs d'échange
        String requestId = (String) exchange.getAttributes().get(RequestIdContext.REQUEST_ID_KEY);

        ResponseDetails responseDetails = new ResponseDetails(
                ex.getMessage(),
                Severity.WARNING, // Sévérité attribuée
                null // Pas d'erreurs sur champs spécifiques donc null
        );

        ApiResult<ResponseDetails> apiResult = new ApiResult<>(
                responseDetails,
            "Conflit de ressource",
            HttpStatus.CONFLICT.value(),
            requestId
        );

        return Mono.just(ResponseEntity
                .status(HttpStatus.CONFLICT) // Code 409
                .body(apiResult));
    }

    @ExceptionHandler(MultipleResourceAlreadyExistException.class)
    public Mono<ResponseEntity<ApiResult<ResponseDetails>>> handleMultipleResourceAlreadyExistException(
            MultipleResourceAlreadyExistException ex, ServerWebExchange exchange) {

        // Récupérer l'ID de requête depuis les attributs d'échange
        String requestId = (String) exchange.getAttributes().get(RequestIdContext.REQUEST_ID_KEY);

        // réponse structurée avec les erreurs des champs
        String msgGeneral = resolveErrorMessage(ex.getMessage(),
                "Un ou plusieurs conflits d'unicité existent");

        // S'assurer que toutes les erreurs de champ ont la sévérité WARNING
        List<FieldInfoDetails> fieldErrors = ex.getFieldErrors().stream()
                .map(fieldError -> new FieldInfoDetails(
                        fieldError.getField(),
                        fieldError.getMessage(),
                        Severity.WARNING
                ))
                .collect(Collectors.toList());

        ResponseDetails responseDetails = new ResponseDetails(msgGeneral, Severity.WARNING, fieldErrors);

        ApiResult<ResponseDetails> apiResult = new ApiResult<>(
                responseDetails,
            "Conflits multiples",
            HttpStatus.CONFLICT.value(),
            requestId
        );

        return Mono.just(ResponseEntity
                .status(HttpStatus.CONFLICT) // Code 409 : Conflit
                .body(apiResult));
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
    public Mono<ResponseEntity<ApiResult<ResponseDetails>>> handleResourceNotFoundException(ResourceNotFoundException ex, ServerWebExchange exchange) {
        // Récupérer l'ID de requête depuis les attributs d'échange
        String requestId = (String) exchange.getAttributes().get(RequestIdContext.REQUEST_ID_KEY);

        ResponseDetails responseDetails = new ResponseDetails(
                ex.getMessage(),
                Severity.INFO, // Sévérité de type "info" attribuée à cette erreur
                null
        );

        ApiResult<ResponseDetails> apiResult = new ApiResult<>(
                responseDetails,
            "Ressource non trouvée",
            HttpStatus.NOT_FOUND.value(),
            requestId
        );

        return Mono.just(ResponseEntity
                .status(HttpStatus.NOT_FOUND) // Code 404
                .body(apiResult));
    }

    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<ApiResult<ResponseDetails>>> handleGeneralException(Exception ex, ServerWebExchange exchange) {
        // Récupérer l'ID de requête depuis les attributs d'échange
        String requestId = (String) exchange.getAttributes().get(RequestIdContext.REQUEST_ID_KEY);

        //Par sécurité, on ne montre pas le détail de l'erreur technique à l'utilisateur
        // on se contente ici de tracer l'erreur avec un log
        ResponseDetails responseDetails = new ResponseDetails(
                "Une erreur interne est survenue. Veuillez réessayer ultérieurement.",
                Severity.ERROR,
                null
        );
        log.error("Une erreur interne est survenue [requestId={}]: ", requestId, ex);

        ApiResult<ResponseDetails> apiResult = new ApiResult<>(
                responseDetails,
            "Erreur interne du serveur",
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            requestId
        );

        return Mono.just(ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR) // Code 500
                .body(apiResult));
    }

    /**
     * Gestion de l'exception IllegalArgumentException.
     *
     * @param ex       L'exception levée
     * @param exchange L'objet ServerWebExchange (injecté automatiquement par Spring Webflux) pour accéder au contexte de la requête
     * @return réponse encapsulée dans ApiResult avec le code d'erreur correspondant
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public Mono<ResponseEntity<ApiResult<ResponseDetails>>> handleIllegalArgumentException(
            IllegalArgumentException ex, ServerWebExchange exchange) {

        // Récupération du `requestId` depuis les attributs d'exchange
        String requestId =  (String) exchange.getAttributes().get(RequestIdContext.REQUEST_ID_KEY);

        // Création d'une réponse formattée avec ResponseDetails et ApiResult
        ResponseDetails responseDetails = new ResponseDetails(
                ex.getMessage(),
                Severity.ERROR
        );

        ApiResult<ResponseDetails> apiResult = new ApiResult<>(
                responseDetails,
                "Requête invalide",
                HttpStatus.BAD_REQUEST.value(),
                requestId
        );

        // Retourner une réponse HTTP 400 (BAD_REQUEST)
        return Mono.just(ResponseEntity.badRequest().body(apiResult));
    }

}
