package com.mdd.auth.models;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Schema(description = "Requête d'inscription")
@Getter
@Setter
public class RegisterRequest {

    @Schema(example = "u1")
    @NotBlank(message = "Le nom d'utilisateur est obligatoire")
    private String username;

    @Schema(example = "u1@test.com")
    @NotBlank(message = "L'adresse e-mail est obligatoire")
    @Email(message = "L'adresse e-mail n'est pas valide")
    private String email;

    @Schema(example = "Test!1234")
    @NotBlank(message = "Le mot de passe est obligatoire")
    @Pattern(
            regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
            message = "Le mot de passe doit contenir au moins 8 caractères, dont au moins 1 lettre majuscule, 1 lettre minuscule, 1 chiffre et 1 caractère spécial."
    )
    private String password;
}
