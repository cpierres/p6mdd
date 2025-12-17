package com.mdd.auth.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table("refresh_tokens")
public class RefreshToken {
    @Id
    private UUID id;

    @Column("user_id")
    private UUID userId;

    private String token;

    @Column("expires_at")
    private Instant expiresAt;

    private boolean revoked;

    @Column("created_at")
    private Instant createdAt;
}
