package com.mdd.back.models;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Schema(description = "Objet représentant l'enregistrement d'un nouvel utilisateur")
@Getter
@Setter
@NoArgsConstructor
public class RegisterRequest {
    @Schema(description = "L'adresse e-mail doit être unique dans le système", example = "cpi@gmail.com")
    @Email(message = "L'adresse e-mail doit être valide")
    @NotBlank(message = "L'adresse e-mail ne peut pas être vide")
    private String email;

    @Schema(description = "Nom et prénom de l'utilisateur", example="Christophe Pierrès")
    @NotBlank(message = "Le nom de l'utilisateur est obligatoire")
    private String username;

    @Schema(description = "Mot de passe", example = "Test!1234")
    @NotBlank(message = "Le mot de passe ne peut pas être vide")
    @Pattern(
            message = "Le mot de passe doit contenir au moins 8 caractères, dont au moins 1 lettre majuscule, 1 lettre minuscule, 1 chiffre et 1 caractère spécial.",
            regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$"
    )
    private String password;
}