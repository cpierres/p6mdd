package com.mdd.back.config;

public class ApiResponseExamples {

    public static final String UNAUTHORIZED_EXAMPLE = """
            {
              "message": "Authentification requise",
              "status": 401,
              "data": {
                "message": "L'utilisateur n'est pas authentifié ou autorisé.",
                "severity": "error"
              },
              "timestamp": "2025-05-05T14:25:34.726938Z",
              "requestId": "60f7396f-df28-4b64-888e-a1932adfe12a"
            }
            """;

    public static final String NOT_FOUND_EXAMPLE = """
            {
              "message": "Ressource non trouvée",
              "status": 404,
              "data": {
                "message": "La ressource demandée n'existe pas.",
                "severity": "info"
              },
              "timestamp": "2025-05-05T14:25:34.726938Z",
              "requestId": "60f7396f-df28-4b64-888e-a1932adfe12a"
            }
            """;

    public static final String BAD_REQUEST_EXAMPLE = """
            {
              "message": "Requête invalide",
              "status": 400,
              "data": {
                "message": "Les données d'entrée ne sont pas valides.",
                "severity": "error",
                "fieldErrors": [
                  {
                    "field": "email",
                    "message": "L'adresse e-mail n'est pas valide",
                    "severity": "error"
                  }
                ]
              },
              "timestamp": "2025-05-05T14:25:34.726938Z",
              "requestId": "60f7396f-df28-4b64-888e-a1932adfe12a"
            }
            """;

    public static final String INTERNAL_SERVER_ERROR_EXAMPLE = """
            {
              "message": "Erreur interne du serveur",
              "status": 500,
              "data": {
                "message": "Une erreur interne est survenue. Veuillez réessayer ultérieurement.",
                "severity": "error"
              },
              "timestamp": "2025-05-05T14:25:34.726938Z",
              "requestId": "60f7396f-df28-4b64-888e-a1932adfe12a"
            }
            """;
}

