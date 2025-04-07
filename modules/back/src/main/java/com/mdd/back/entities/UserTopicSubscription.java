package com.mdd.back.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(value = "user_topic_subscription", schema = "mddsocial")
public class UserTopicSubscription {

    @Id
    private UUID id;
    private UUID userId;
    private UUID topicId;
}
