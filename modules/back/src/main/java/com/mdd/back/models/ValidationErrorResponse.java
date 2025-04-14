package com.mdd.back.models;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;


/**
 * Représente une réponse contenant des erreurs de validation.
 *
 * Cette classe est utilisée pour transmettre des informations détaillées
 * concernant les erreurs rencontrées lors de la validation des données d'entrée
 * dans les requêtes.
 *
 * Attributs :
 * - `message` : Spécifie un message général de validation, par exemple "Les données d'entrée ne sont pas valides".
 * - `fieldErrors` : Contient une collection clé-valeur où chaque clé correspond à un champ non conforme,
 *   et chaque valeur représente le message d'erreur associé, par exemple :
 *   { "password": "Le mot de passe ne peut pas être vide", "email": "L'adresse e-mail doit être valide" }.
 */
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

