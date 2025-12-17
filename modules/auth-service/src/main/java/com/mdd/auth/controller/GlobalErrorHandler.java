package com.mdd.auth.controller;

import com.mdd.auth.models.ApiResult;
import com.mdd.auth.models.ResponseDetails;
import com.mdd.auth.models.Severity;
import com.mdd.auth.services.AuthService;
import com.mdd.auth.utils.RequestIdFilter;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ServerWebExchange;

@Slf4j
@RestControllerAdvice
public class GlobalErrorHandler {

    @ExceptionHandler(AuthService.MultipleConflictException.class)
    public ResponseEntity<ApiResult<ResponseDetails>> handleMultipleConflicts(AuthService.MultipleConflictException ex, ServerWebExchange exchange) {
        String requestId = (String) exchange.getAttributes().get(RequestIdFilter.REQUEST_ID_KEY);
        ResponseDetails details = new ResponseDetails(
                "Conflit(s) : " + ex.getFields().stream().map(f -> f.getField() + ": " + f.getMessage()).reduce((a, b) -> a + " " + b).orElse(""),
                Severity.WARNING
        );
        details.setFieldErrors(ex.getFields());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ApiResult<>(details, "Conflits multiples", HttpStatus.CONFLICT.value(), requestId));
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, ConstraintViolationException.class})
    public ResponseEntity<ApiResult<ResponseDetails>> handleValidation(Exception ex, ServerWebExchange exchange) {
        String requestId = (String) exchange.getAttributes().get(RequestIdFilter.REQUEST_ID_KEY);
        ResponseDetails details = new ResponseDetails("Les données d'entrée ne sont pas valides.", Severity.ERROR);
        return ResponseEntity.badRequest()
                .body(new ApiResult<>(details, "Erreur de validation", HttpStatus.BAD_REQUEST.value(), requestId));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResult<ResponseDetails>> handleOther(Exception ex, ServerWebExchange exchange) {
        String requestId = (String) exchange.getAttributes().get(RequestIdFilter.REQUEST_ID_KEY);
        // Logguer l'exception avec le requestId pour faciliter le debug en dev.
        // (Les détails ne sont pas renvoyés au client.)
        log.error("Erreur non gérée (requestId={})", requestId, ex);
        ResponseDetails details = new ResponseDetails("Une erreur interne est survenue. Veuillez réessayer ultérieurement.", Severity.ERROR);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResult<>(details, "Erreur interne", HttpStatus.INTERNAL_SERVER_ERROR.value(), requestId));
    }
}
