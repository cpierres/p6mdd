package com.mdd.back.services;

import com.mdd.back.config.JwtProperties;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtService {
    @Value("${jwt.secret-key}")
    private String BASE64_SECRET;

    //@Value("${JWT_EXPIRATION_TIME}")
    @Value("${jwt.expiration-time}")
    private Integer JWT_EXPIRATION_TIME;

    private SecretKey SECRET_KEY;

    @PostConstruct
    public void initializeSecretKey() {
        SECRET_KEY = new SecretKeySpec(
                Base64.getDecoder().decode(BASE64_SECRET),
                SignatureAlgorithm.HS256.getJcaName()
        );
    }

    /**
     * Générer un token JWT en y incluant le username (classique) mais aussi son id,
     * afin de pouvoir retrouver/extraire ce dernier à partir de tout traitement.
     * @param id identifiant unique de l'utilisateur
     * @param username son adresse email unique
     * @return token crypté en HS256
     */
    public String generateToken(UUID id, String username) {
        return Jwts.builder()
                .setSubject(username) // Nom d'utilisateur (claim "sub")
                .claim("id", id)      // Claim personnalisé pour inclure l'ID du user nécessaire
                .setIssuedAt(new Date()) // Date d'émission
                .setExpiration(new Date(System.currentTimeMillis() + JWT_EXPIRATION_TIME * 1000)) // Expiration
                .signWith(SECRET_KEY) // Signature avec clé secrète
                .compact();
    }

    /**
     * Renvoyer la clé pour la configuration côté Resource Server
     * @return
     */
    public SecretKey getSecretKey() {
        return SECRET_KEY;
    }
}
