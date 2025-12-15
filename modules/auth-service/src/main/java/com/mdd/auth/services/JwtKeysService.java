package com.mdd.auth.services;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.UUID;

/**
 * Service responsable de la génération/gestion des clés JWT (RSA) et
 * de la publication du JWK Set. Pour l'instant, les clés sont générées
 * en mémoire au démarrage (suffisant pour un squelette de service).
 * En production, prévoir un stockage sécurisé et la rotation des clés.
 */
@Slf4j
@Service
public class JwtKeysService {

    @Getter
    private final RSAKey rsaJwk;

    @Getter
    private final JWKSet jwkSet;

    public JwtKeysService() {
        try {
            // Générer une paire de clés RSA 2048
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
            kpg.initialize(2048);
            KeyPair kp = kpg.generateKeyPair();
            RSAPublicKey publicKey = (RSAPublicKey) kp.getPublic();
            RSAPrivateKey privateKey = (RSAPrivateKey) kp.getPrivate();

            // Assigner un kid unique pour permettre la rotation ultérieure
            String kid = UUID.randomUUID().toString();

            this.rsaJwk = new RSAKey.Builder(publicKey)
                    .privateKey(privateKey)
                    .keyID(kid)
                    .build();

            this.jwkSet = new JWKSet(rsaJwk.toPublicJWK());
            log.info("[Auth-Service] Clé RSA générée avec kid={}", kid);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Impossible de générer les clés RSA", e);
        }
    }
}
