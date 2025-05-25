package com.mdd.back.models;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

//@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
//@JsonSubTypes({
//        @JsonSubTypes.Type(value = AuthSuccess.class, name = "authSuccess"),
//        @JsonSubTypes.Type(value = ResponseDetails.class, name = "errorResponse"),
//        @JsonSubTypes.Type(value = UserDto.class, name = "userDto")
//})
@Schema(description = "Structure de réponse générique pour unifier et rendre flexible les retours d'une API.")
@Getter
@Setter
@AllArgsConstructor
public class ApiResult<T> {

    @Schema(description = "Message général décrivant la réponse, par exemple 'Opération réussie' ou une erreur explicite.",
            example = "Message général décrivant la réponse.")
    private String message;

    @Schema(description = "Code HTTP associé à cette réponse.",
            example = "500 OU 200 ou 401")
    private int status;

    @Schema(
            description = "Les données de la réponse. Par exemple, un token en cas de succès ou null en cas d’erreur. Data peut être null",
            oneOf = {ResponseDetails.class, AuthSuccess.class, UserDto.class} //classes possibles pour ce champ
    )
    private T data;

    @Schema(description = "Horodatage de la réponse au format ISO 8601.",
            example = "2025-05-05T14:25:34.726938Z")
    private Instant timestamp;

    @Schema(description = "Identifiant unique de la requête, utile pour le suivi et le débogage.",
            example = "60f7396f-df28-4b64-888e-a1932adfe12a")
    private String requestId;

    // Constructeur existant
    public ApiResult(T data, String message, int status) {
        this.data = data;
        this.message = message;
        this.status = status;
        this.timestamp = Instant.now();
        this.requestId = UUID.randomUUID().toString();
    }

    // constructeur avec requestId explicite (transmis grâce à RequestIdFilter)
    public ApiResult(T data, String message, int status, String requestId) {
        this.data = data;
        this.message = message;
        this.status = status;
        this.timestamp = Instant.now();
        this.requestId = requestId;
    }

}
