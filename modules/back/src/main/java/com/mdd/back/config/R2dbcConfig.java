package com.mdd.back.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.r2dbc.config.EnableR2dbcAuditing;

@Configuration
@EnableR2dbcAuditing
public class R2dbcConfig {
    // Activates auditing for R2DBC (createdAt, updatedAt auto-management)
}