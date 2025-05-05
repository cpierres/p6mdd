package com.mdd.back.models;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


/**
 * Représente une réponse contenant des erreurs de validation.
 * <p>
 * Cette classe est utilisée pour transmettre des informations détaillées
 * concernant les erreurs rencontrées lors de la validation des données d'entrée
 * dans les requêtes.
 * <p>
 * Attributs :
 * - `message` : Spécifie un message général de validation, par exemple "Les données d'entrée ne sont pas valides".
 * - `fieldErrors` : Contient une collection clé-valeur où chaque clé correspond à un champ non conforme,
 *   et chaque valeur représente le message d'erreur associé, par exemple :
 *   {"password": "Le mot de passe ne peut pas être vide", "email": "L'adresse e-mail doit être valide" }.
 */
@Schema(
        name = "ValidationErrorResponse",
        description = "Représente la structure standard de la réponse retournée lorsqu'une validation échoue dans une requête API. Ce modèle fournit des informations détaillées sur les erreurs détectées, leur champ, leur message d'erreur, et leur niveau de gravité."
)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ValidationErrorResponse {
    @Schema(description = "Message général de validation", example = "Les données d'entrée ne sont pas valides")
    private String message;

    @Schema(description = "Niveau de sévérité de l'erreur", example = "WARNING", allowableValues = {"INFO", "WARNING", "ERROR", "SUCCESS"}
    )
    private Severity severity;

    @Schema(description = "Liste des détails des erreurs des champs",
            example = "[ { \"field\": \"password\", \"message\": \"Le mot de passe est trop court\", \"severity\": \"error\" } ]"
    )
    private List<FieldInfoDetails> fieldErrors; // Liste des erreurs détaillées par champ

    public ValidationErrorResponse(String message, List<FieldInfoDetails> fieldErrors) {
        this.message = message;
        this.fieldErrors = fieldErrors;
        this.severity = Severity.ERROR; // Valeur par défaut
    }

}

