package com.mdd.back.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(value = "topics", schema = "mddsocial")
public class Topic {
    @Id
    private UUID id;
    private String title;
    private String description;
    private double priorityOrder;

//    // Liste des utilisateurs abonnés
//    private Set<UUID> subscribedUsers;

}