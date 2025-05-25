package com.mdd.back.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
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
                                        .bearerFormat("JWT"))// Indique que le token est de type JWT
                        // Réponses d'erreur standard
                        .addResponses("Unauthorized", createUnauthorizedResponse())
                        .addResponses("NotFound", createNotFoundResponse())
                        .addResponses("BadRequest", createBadRequestResponse())
                        .addResponses("InternalServerError", createInternalServerErrorResponse())
                );

    }

    private ApiResponse createUnauthorizedResponse() {
        return new ApiResponse()
                .description("L'utilisateur n'est pas authentifié ou autorisé.")
                .content(new Content()
                        .addMediaType("application/json",
                                new MediaType().schema(new Schema<>().$ref("#/components/schemas/ApiResult"))));
    }

    private ApiResponse createNotFoundResponse() {
        return new ApiResponse()
                .description("Ressource non trouvée.")
                .content(new Content()
                        .addMediaType("application/json",
                                new MediaType().schema(new Schema<>().$ref("#/components/schemas/ApiResult"))));
    }

    private ApiResponse createBadRequestResponse() {
        return new ApiResponse()
                .description("Requête invalide ou données mal formatées.")
                .content(new Content()
                        .addMediaType("application/json",
                                new MediaType().schema(new Schema<>().$ref("#/components/schemas/ApiResult"))));
    }

    private ApiResponse createInternalServerErrorResponse() {
        return new ApiResponse()
                .description("Erreur interne du serveur.")
                .content(new Content()
                        .addMediaType("application/json",
                                new MediaType().schema(new Schema<>().$ref("#/components/schemas/ApiResult"))));
    }

}

