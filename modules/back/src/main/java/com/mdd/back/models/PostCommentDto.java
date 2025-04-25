package com.mdd.back.models;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostCommentDto {
    private UUID id;

    @NotNull(message = "L'identifiant du post est obligatoire")
    private UUID postId;

    @NotBlank(message = "Le commentaire est obligatoire")
    private String comment;

    private Instant createdAt;
    private Instant updatedAt;
    private UUID createdBy;
    private String createdByUsername;
}
