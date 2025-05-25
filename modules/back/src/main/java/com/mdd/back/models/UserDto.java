package com.mdd.back.models;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Schema(description = "Structure représentant les informations détaillées d'un utilisateur.")
@Getter
@Setter
@RequiredArgsConstructor
public class UserDto {

    @Schema(description = "Identifiant unique de l'utilisateur.", example = "d1a27f64-403d-4c27-9fb7-1b54168a546d")
    private UUID id;

    @Schema(description = "Nom de l'utilisateur. Doit être unique car connexion possible via le nom.",
            example = "u1")
    private String username;

    @Schema(description = "Adresse email associée à l'utilisateur. Doit être unique",
            example = "u1@test.com")
    @Email(message = "L'adresse e-mail doit être valide")
    @NotBlank(message = "L'adresse e-mail ne peut pas être vide")
    private String email;

    @Schema(description = "Date/heure de création en lecture seule car gérée par le système")
    private Instant created_at;

    @Schema(description = "Date/heure de mise à jour gérée par le système")
    private Instant updated_at;

}
