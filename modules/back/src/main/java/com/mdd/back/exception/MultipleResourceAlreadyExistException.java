package com.mdd.back.exception;

import com.mdd.back.models.FieldInfoDetails;
import com.mdd.back.models.Severity;
import lombok.Getter;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
public class MultipleResourceAlreadyExistException extends RuntimeException {

    private final List<FieldInfoDetails> fieldErrors; // Liste détaillée des erreurs des champs

    /**
     * Constructeur utilisant une liste détaillée d'erreurs.
     *
     * @param fieldErrors Liste des détails des erreurs de champs.
     */
    public MultipleResourceAlreadyExistException(List<FieldInfoDetails> fieldErrors) {
        super("Conflit(s) :\n" + fieldErrorsToString(fieldErrors));
        //this.fieldErrors = fieldErrors;
        // S'assurer que toutes les erreurs sans sévérité définie reçoivent une sévérité warning par défaut
        this.fieldErrors = fieldErrors.stream()
                .map(fieldError -> {
                    if (fieldError.getSeverity() == null) { // Si la sévérité est absente
                        return new FieldInfoDetails(
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
                .map(entry -> new FieldInfoDetails(entry.getKey(), entry.getValue(), Severity.WARNING))
                .collect(Collectors.toList()));
    }

    /**
     * Transforme les erreurs détaillées en une chaîne lisible.
     *
     * @param fieldErrors Liste des détails des erreurs de champs.
     * @return Chaîne représentant les erreurs.
     */
    private static String fieldErrorsToString(List<FieldInfoDetails> fieldErrors) {
        return fieldErrors.stream()
                .map(detail -> detail.getField() + ": " + detail.getMessage())
                .collect(Collectors.joining("\n"));
    }
}
