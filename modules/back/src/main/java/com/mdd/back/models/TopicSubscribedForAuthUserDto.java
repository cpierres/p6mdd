package com.mdd.back.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TopicSubscribedForAuthUserDto {
    private UUID id;
    private String title;
    private String description;
    private Boolean subscribed; // si utilisateur abonné
    private Long countPosts;    // nb posts associés au topic en temps réel (via SSE)
    private Long countComments; // temps réel (via SSE)
    private double priorityOrder; // ordre de priorité pour le tri secondaire
}
