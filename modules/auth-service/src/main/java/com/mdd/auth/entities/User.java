package com.mdd.auth.entities;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.annotation.Transient;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.data.domain.Persistable;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Table("users")
public class User implements Persistable<UUID> {

    @Id
    private UUID id;

    private String username;
    private String email;

    @Column("password")
    private String passwordHash;

    // Mapping explicite vers les colonnes snake_case de la table (évite les erreurs de mapping selon la stratégie de nommage)
    @Column("created_at")
    private Instant createdAt;

    @Column("updated_at")
    private Instant updatedAt;

    @Transient
    private boolean isNew = false;

    /**
     * Constructeur utilisé par Spring Data lors de la lecture depuis la base.
     */
    @PersistenceCreator
    public User(UUID id, String username, String email, String passwordHash, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.isNew = false;
    }

    /**
     * Constructeur de création : force un INSERT même si un id est renseigné par erreur.
     */
    public static User newUser(String username, String email, String passwordHash, Instant createdAt, Instant updatedAt) {
        User u = new User(null, username, email, passwordHash, createdAt, updatedAt);
        u.isNew = true;
        return u;
    }

    @Override
    public boolean isNew() {
        return isNew || id == null;
    }
}
