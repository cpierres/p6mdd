package com.mdd.back.models;

import lombok.Data;

/**
 * Modèle représentant une erreur, un warning ou une info liée à un champ spécifique,
 * avec un niveau de sévérité associé.
 * Cette classe est utilisée par {@link ResponseDetails} qui est la classe générique pour gérer les retours/erreurs
 * vis-à-vis du frontend.
 */
@Data
public class FieldInfoDetails {
    private String field;       // Nom du champ associé à l'erreur
    private String message;     // Message décrivant l'erreur
    private Severity severity;    // Niveau de sévérité : error, warning, info

    // Constructeur par défaut avec une valeur initiale pour severity
    public FieldInfoDetails(String field, String message) {
        this.field = field;
        this.message = message;
        this.severity = Severity.ERROR; // Valeur par défaut
    }

    // Constructeur dédié si une sévérité spécifique est fournie
    public FieldInfoDetails(String field, String message, Severity severity) {
        this.field = field;
        this.message = message;
        this.severity = severity;
    }

}

