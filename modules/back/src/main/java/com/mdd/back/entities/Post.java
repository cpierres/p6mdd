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
@Table(value = "post", schema = "mddsocial")
public class Post extends BaseEntity {
    @Id
    private UUID id;

    @Column("topic_id")
    private UUID topicId;

    private String title;

    private String content;

    @Column("created_by")
    private UUID createdBy;
}