package com.mdd.auth.models;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Schema(description = "Requête de connexion")
@Getter
@Setter
public class LoginRequest {

    @Schema(description = "Email ou username", example = "u1@test.com")
    @NotBlank(message = "L'adresse e-mail ou le nom d'utilisateur doivent être renseignés")
    private String identifier;

    @Schema(description = "Mot de passe", example = "Test!1234")
    @NotBlank(message = "Le mot de passe ne peut pas être vide.")
    @Pattern(
            regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
            message = "Le mot de passe doit contenir au moins 8 caractères, dont au moins 1 lettre majuscule, 1 lettre minuscule, 1 chiffre et 1 caractère spécial."
    )
    private String password;
}
