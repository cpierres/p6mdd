package com.mdd.back.entities;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(value = "post_comment", schema = "mddsocial")
public class PostComment extends BaseEntity {
    @Id
    private UUID id;

    @Column("post_id")
    private UUID postId;

    private String comment;

    @Column("created_by")
    private UUID createdBy;
}
