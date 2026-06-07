package fr.miage.numres.common;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.Map;

/*
dto standardisé pour les réponses d'erreur de l'API
Partagé par les deux domaines (catalog, watchlist)
*/
@Schema(description = "Réponse d'erreur normalisée de l'API")
public record ApiError(
        @Schema(description = "Instant de l'erreur") Instant timestamp,
        @Schema(description = "Code HTTP", example = "404") int status,
        @Schema(description = "Libellé du statut HTTP", example = "Not Found") String error,
        @Schema(description = "Message lisible") String message,
        @Schema(description = "Chemin de la requête", example = "/api/catalog/999") String path,
        @Schema(description = "Erreurs de validation par champ (éventuellement vide)") Map<String, String> fieldErrors
) {
}
