package com.mdd.auth.models;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Détails d'une réponse (erreur/info)")
public class ResponseDetails {

    private String message;
    private Severity severity;

    @ArraySchema(schema = @Schema(description = "Erreurs par champ"))
    private List<FieldInfoDetails> fieldErrors;

    public ResponseDetails(String message, Severity severity) {
        this.message = message;
        this.severity = severity;
        this.fieldErrors = null;
    }

    public ResponseDetails(String message, List<FieldInfoDetails> fieldErrors) {
        this.message = message;
        this.fieldErrors = fieldErrors;
        this.severity = Severity.ERROR;
    }
}
