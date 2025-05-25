package com.mdd.back.models;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostDto {
    private UUID id;

    @NotBlank(message = "Le titre est obligatoire")
    private String title;

    @NotNull(message = "Le thème est obligatoire")
    private UUID topicId;

    private String topicTitle;

    @NotBlank(message = "Le contenu est obligatoire")
    private String content;

    private Instant createdAt;
    private Instant updatedAt;
    private UUID createdBy;
    private String createdByUsername;

    //est-ce que l'utilisateur connecté peut modifier le post ?
    private boolean updatable;

    // Liste optionnelle de commentaires
    private List<PostCommentDto> comments;
}
