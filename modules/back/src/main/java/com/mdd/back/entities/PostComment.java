package com.mdd.back.entities;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.UUID;

@Data
//@SuperBuilder
@Builder
@Table(value = "post_comment", schema = "mddsocial")
//public class PostComment extends BaseEntity {
public class PostComment {
    @Id
    private UUID id;

    @Column("post_id")
    private UUID postId;

    private String comment;

    @CreatedDate
    @Column("created_at")
    private Instant createdAt;

    @LastModifiedDate
    @Column("updated_at")
    private Instant updatedAt;

    @CreatedBy
    @Column("created_by")
    private UUID createdBy;
}
