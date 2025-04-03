package com.mdd.back.models;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Schema(description = """
        Contient le token d'authentification renvoyé en réponse
        - à une requête valide sur 'GET /api/login
        - ou l'enregistrement d'un nouvel utilisateur via POST /api/auth/register
        """)
@Getter
@Setter
@AllArgsConstructor
public class AuthSuccess {
    private String token;
}
