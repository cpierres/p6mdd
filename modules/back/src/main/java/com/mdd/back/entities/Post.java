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

//@EqualsAndHashCode(callSuper = true)
@Data
//@SuperBuilder
@Builder
@Table(value = "post", schema = "mddsocial")
//public class Post extends BaseEntity {
public class Post {
    @Id
    private UUID id;

    @Column("topic_id")
    private UUID topicId;

    private String title;

    private String content;

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