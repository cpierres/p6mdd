package com.mdd.auth.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.Map;

/**
 * Squelette d'API d'authentification. Les méthodes retournent pour l'instant
 * HTTP 501 (Not Implemented). Elles seront complétées avec la logique
 * d'inscription, de login, de refresh et de logout (tokens JWT + cookie HttpOnly)
 * conformément aux spécifications.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, Object> body) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED)
                .body(Collections.singletonMap("message", "register: à implémenter"));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, Object> body) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED)
                .body(Collections.singletonMap("message", "login: à implémenter"));
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh() {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED)
                .body(Collections.singletonMap("message", "refresh: à implémenter"));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED)
                .body(Collections.singletonMap("message", "logout: à implémenter"));
    }
}
