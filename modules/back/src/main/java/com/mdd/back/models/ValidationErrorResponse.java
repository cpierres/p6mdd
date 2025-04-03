package com.mdd.back.models;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ValidationErrorResponse {
    @Schema(description = "Message général de validation", example = "Les données d'entrée ne sont pas valides")
    private String message;

    @Schema(description = "Pour un champ donnée, affiche une erreur",
            example = "{ \"password\": \"Le mot de passe ne peut pas être vide\", \"email\": \"L'adresse e-mail doit être valide\" }"
    )
    private Map<String, String> fieldErrors;
}

