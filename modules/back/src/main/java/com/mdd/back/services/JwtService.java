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
     * 
     * Cette méthode fait partie de la logique du serveur d'autorisation dans l'architecture OAuth2.
     * Elle est appelée lors de l'authentification d'un utilisateur (/api/auth/login) ou de son
     * enregistrement (/api/auth/register) pour générer un token JWT qui sera ensuite utilisé
     * pour authentifier les requêtes ultérieures.
     * 
     * Dans notre architecture de "serveur de ressources OAuth2 autonome", cette méthode
     * représente la fonctionnalité d'émission de tokens du serveur d'autorisation.
     * 
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
     * Renvoyer la clé secrète pour la configuration côté Resource Server.
     * 
     * Cette méthode joue un rôle crucial dans la coordination entre le serveur d'autorisation
     * et le serveur de ressources dans notre architecture de "serveur de ressources OAuth2 autonome".
     * 
     * Elle permet au serveur de ressources (configuré via oauth2ResourceServer) d'accéder à la
     * même clé secrète que celle utilisée par le serveur d'autorisation pour générer les tokens.
     * Ainsi, le serveur de ressources peut valider l'authenticité des tokens émis par le
     * serveur d'autorisation.
     * 
     * Cette méthode est utilisée par les beans ReactiveJwtDecoder et JwtEncoder dans SecurityConfig.
     * 
     * @return La clé secrète utilisée pour signer et valider les tokens JWT
     */
    public SecretKey getSecretKey() {
        return SECRET_KEY;
    }
}
