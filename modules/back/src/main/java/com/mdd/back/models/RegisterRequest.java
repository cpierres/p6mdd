package com.mdd.back.models;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Schema(description = "Objet représentant l'enregistrement d'un nouvel utilisateur")
@Getter
@Setter
@NoArgsConstructor
public class RegisterRequest {
    @Schema(description = "L'adresse e-mail doit être unique dans le système", example = "u1@test.com")
    @Email(message = "L'adresse e-mail doit être valide")
    @NotBlank(message = "L'adresse e-mail ne peut pas être vide")
    private String email;

    @Schema(description = "Nom de l'utilisateur", example="u1")
    @NotBlank(message = "Le nom de l'utilisateur est obligatoire")
    @Size(min = 2, message = "Le nom d'utilisateur doit contenir au moins 2 caractères")
    private String username;

    @Schema(description = "Mot de passe", example = "Test!1234")
    @NotBlank(message = "Le mot de passe ne peut pas être vide")
    @Size(min = 8, message = "Le mot de passe doit contenir au moins 8 caractères")
    @Pattern(
            message = "Le mot de passe doit contenir au moins 8 caractères, dont au moins 1 lettre majuscule, 1 lettre minuscule, 1 chiffre et 1 caractère spécial.",
            regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$"
    )
    private String password;
}
