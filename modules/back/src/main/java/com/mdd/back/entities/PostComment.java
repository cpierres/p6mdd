package com.mdd.back.entities;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@Table(value = "post_comment", schema = "mddsocial")
public class PostComment extends BaseEntity {
    @Id
    private UUID id;

    @Column("post_id")
    private UUID postId;

    private String comment;

}
