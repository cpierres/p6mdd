package com.mdd.back.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.SecurityScheme.Type;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI myOpenApi() {
        // Nom du schéma de sécurité (utilisé dans les requêtes)
        String securitySchemeName = "Bearer Authentication";

        return new OpenAPI()
                .info(new Info()
                        .title("MDD API")
                        .description("Documentation de l'API pour le réseau social MDD (Monde des développeurs)")
                        .version("v1.0.0")
                        .license(new License()
                                .name("Spring Framework: Apache License 2.0, MySQL: GPLv2")
                                .url("https://www.apache.org/licenses/LICENSE-2.0"))
                        .contact(new Contact()
                                .name("Christophe Pierrès")
                                .email("cpierres[at]hotmail.com")
                        )
                )
//                .addSecurityItem(new SecurityRequirement()
//                        .addList(securitySchemeName)) // Applique le schéma à toutes les requêtes
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT"))); // Indique que le token est de type JWT

    }
}

