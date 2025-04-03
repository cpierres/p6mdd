package com.mdd.back.entities;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table("users")
public class User extends BaseEntity {
    @Id
    private UUID id;

    private String email;

    private String password;

    private String username;
}