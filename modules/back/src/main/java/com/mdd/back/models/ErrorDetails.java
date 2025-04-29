package com.mdd.back.models;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

/**
 * Représente les détails d'une erreur dans l'application.
 * <p>
 * Cette classe est utilisée pour transmettre des informations détaillées sur les erreurs survenues
 * lors de l'exécution de l'application, notamment un message général, le niveau de sévérité et
 * les erreurs spécifiques associées aux champs.
 */
@Getter
@AllArgsConstructor
@Schema(
        description = "Modèle pour représenter les informations d'une erreur survenue dans l'application. " +
                "L'erreur peut se limiter à un message général avec une sévérité, ou bien inclure également " +
                "des détails sur des champs en erreur."
)
public class ErrorDetails {

    @Schema(
            description = "Message général décrivant l'erreur.",
            example = "Une erreur est survenue lors du traitement de la requête."
    )
    private String message;

    @Schema(
            description = "Niveau de sévérité de l'erreur.",
            example = "ERROR",
            allowableValues = {"ERROR", "WARNING", "INFO", "SUCCESS"}
    )
    private Severity severity;

    @ArraySchema(
            schema = @Schema(
                    description = "Liste des champs en erreur (facultatif). Chaque entrée contient des informations détaillées " +
                            "telles que le nom du champ, un message décrivant le problème, et une sévérité (optionnelle).",
                    example = """
                            [
                              { "field": "email", "message": "L'adresse e-mail n'est pas valide", "severity": "warning" },
                              { "field": "password", "message": "Le mot de passe est trop court" }
                            ]"""),
            arraySchema = @Schema(
                    description = "Chaque entrée contient des informations détaillées telles que le nom du champ, un message décrivant le problème, et une sévérité spécifique."
            )
    )

    private List<FieldErrorDetail> fieldErrors;
}