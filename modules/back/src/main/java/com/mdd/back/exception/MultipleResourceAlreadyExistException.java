package com.mdd.back.exception;

import lombok.Getter;

import java.util.Map;

@Getter
public class MultipleResourceAlreadyExistException extends RuntimeException {
    private final Map<String, String> fieldErrors; // Association champ -> message d'erreur

    public MultipleResourceAlreadyExistException(Map<String, String> fieldErrors) {
        super("Un ou plusieurs champs sont en conflit : " + fieldErrorsToString(fieldErrors));
        this.fieldErrors = fieldErrors;
    }

    private static String fieldErrorsToString(Map<String, String> fieldErrors) {
        StringBuilder sb = new StringBuilder();
        fieldErrors.forEach((field, message) ->
                sb.append(field).append(": ").append(message).append(" | "));
        return sb.toString();
    }
}