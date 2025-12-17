package com.mdd.auth.models;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Schema(description = "Contient le token JWT renvoyé à la connexion/inscription/refresh")
@Getter
@Setter
@AllArgsConstructor
public class AuthSuccess {
    private String token;
}
