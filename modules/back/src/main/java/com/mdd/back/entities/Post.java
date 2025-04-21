package com.mdd.back.entities;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@Table(value = "post", schema = "mddsocial")
public class Post extends BaseEntity {
    @Id
    private UUID id;

    @Column("topic_id")
    private UUID topicId;

    private String title;

    private String content;
}