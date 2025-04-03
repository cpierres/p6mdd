package com.mdd.back.models;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
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

    @Schema(description = "Mot de passe", example = "pw")
    @NotBlank(message = "Le mot de passe ne peut pas être vide")
    private String password;
}