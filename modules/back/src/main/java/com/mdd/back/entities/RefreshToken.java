package com.mdd.back.entities;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.UUID;

/**
 * Entité représentant un refresh token stocké en base de données.
 * Utilisé pour permettre à un utilisateur de rafraîchir son access token
 * sans avoir à se reconnecter.
 */
@Data
@Table("mddsocial.refresh_tokens")
public class RefreshToken {
    @Id
    private UUID id;
    
    @Column("user_id")
    private UUID userId;
    
    @Column("token")
    private String token;
    
    @Column("expiry_date")
    private Instant expiryDate;
}