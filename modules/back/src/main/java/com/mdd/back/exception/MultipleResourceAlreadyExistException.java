package com.mdd.back.exception;

import com.mdd.back.models.FieldErrorDetail;
import com.mdd.back.models.Severity;
import lombok.Getter;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
public class MultipleResourceAlreadyExistException extends RuntimeException {

    private final List<FieldErrorDetail> fieldErrors; // Liste détaillée des erreurs des champs

    /**
     * Constructeur utilisant une liste détaillée d'erreurs.
     *
     * @param fieldErrors Liste des détails des erreurs de champs.
     */
    public MultipleResourceAlreadyExistException(List<FieldErrorDetail> fieldErrors) {
        super("Un ou plusieurs champs sont en conflit : " + fieldErrorsToString(fieldErrors));
        //this.fieldErrors = fieldErrors;
        // S'assurer que toutes les erreurs sans sévérité définie reçoivent une sévérité warning par défaut
        this.fieldErrors = fieldErrors.stream()
                .map(fieldError -> {
                    if (fieldError.getSeverity() == null) { // Si la sévérité est absente
                        return new FieldErrorDetail(
                                fieldError.getField(),
                                fieldError.getMessage(),
                                Severity.WARNING
                        );
                    }
                    return fieldError; // Sinon, garder tel quel
                })
                .collect(Collectors.toList());
    }

    /**
     * Constructeur utilisant une Map (champ -> message), avec une sévérité par défaut.
     *
     * @param fieldErrors Map contenant les champs et leurs messages d'erreur.
     */
    public MultipleResourceAlreadyExistException(Map<String, String> fieldErrors) {
        this(fieldErrors.entrySet().stream()
                .map(entry -> new FieldErrorDetail(entry.getKey(), entry.getValue(), Severity.WARNING))
                .collect(Collectors.toList()));
    }

    /**
     * Transforme les erreurs détaillées en une chaîne lisible.
     *
     * @param fieldErrors Liste des détails des erreurs de champs.
     * @return Chaîne représentant les erreurs.
     */
    private static String fieldErrorsToString(List<FieldErrorDetail> fieldErrors) {
        return fieldErrors.stream()
                .map(detail -> detail.getField() + ": " + detail.getMessage())
                .collect(Collectors.joining(" | "));
    }
}
