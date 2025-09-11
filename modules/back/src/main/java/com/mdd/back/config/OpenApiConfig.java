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
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Value("#{'${frontend.url}'.split(',')}")
    private List<String> frontendUrls;

    //propriété spécifique pour l'URL de l'API
    @Value("${api.base.url:https://apimdd.cpierres.dscloud.me}")
    private String apiBaseUrl;


    @Bean
    public OpenAPI myOpenApi() {
        // Nom du schéma de sécurité (utilisé dans les requêtes)
        String securitySchemeName = "Bearer Authentication";

        // Créer une instance OpenAPI avec les serveurs configurés
        OpenAPI openAPI = new OpenAPI()
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
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT"))
                        .addResponses("Unauthorized", createUnauthorizedResponse())
                        .addResponses("NotFound", createNotFoundResponse())
                        .addResponses("BadRequest", createBadRequestResponse())
                        .addResponses("InternalServerError", createInternalServerErrorResponse())
                );

        // Ajouter les serveurs en utilisant les URLs frontend
//        for (String url : frontendUrls) {
//            // Convertir explicitement en HTTPS si c'est une URL externe (pas localhost)
//            if (url.contains("http://") && !url.contains("localhost")) {
//                url = url.replace("http://", "https://");
//            }
//            openAPI.addServersItem(new Server().url(url));
//        }

        // Ajouter le serveur API (pas le frontend !)
        openAPI.addServersItem(new Server()
                .url(apiBaseUrl)
                .description("API Server"));

        // localhost pour le développement
        openAPI.addServersItem(new Server()
                .url("http://localhost:8080")
                .description("Development Server"));

        return openAPI;
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

