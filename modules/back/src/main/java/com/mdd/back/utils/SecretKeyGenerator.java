package com.mdd.back.utils;

import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;

public class SecretKeyGenerator {
    public static void main(String[] args) {
        // Génère une clé secrète pour HS256
        SecretKey secretKey = Keys.secretKeyFor(SignatureAlgorithm.HS256);

        // Affiche la clé sous forme encodée (base64) pour faciliter l'utilisation
        String base64EncodedKey = java.util.Base64.getEncoder().encodeToString(secretKey.getEncoded());

        System.out.println("Clé secrète pour HS256 (encodée en Base64) : " + base64EncodedKey);
    }
}