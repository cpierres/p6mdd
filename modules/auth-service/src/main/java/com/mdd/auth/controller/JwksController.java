package com.mdd.auth.controller;

import com.mdd.auth.services.JwtKeysService;
import com.nimbusds.jose.jwk.JWKSet;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Contrôleur exposant le JWK Set public à l'URL standard
 * "/.well-known/jwks.json" pour permettre aux Resource Servers
 * (ex: modules/back) de valider les JWT signés par ce service.
 */
@RestController
@RequiredArgsConstructor
public class JwksController {

    private final JwtKeysService jwtKeysService;

    @GetMapping(value = "/.well-known/jwks.json", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> jwks() {
        JWKSet jwkSet = jwtKeysService.getJwkSet();
        return jwkSet.toJSONObject();
    }
}
