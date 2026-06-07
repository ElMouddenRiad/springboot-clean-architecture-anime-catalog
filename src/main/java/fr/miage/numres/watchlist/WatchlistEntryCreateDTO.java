package fr.miage.numres.watchlist;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/*
dto pour l'ajout d'un anime à la watchlist POST
userId est optionnel (un user de démonstration est appliqué par défaut), status et 
currentEpisode ont aussi des valeurs par défaut côté service s'ils sont oubliés
*/
@Schema(description = "Données pour ajouter un anime à la watchlist")
public record WatchlistEntryCreateDTO(

        @Schema(description = "Identifiant du propriétaire (optionnel, défaut : utilisateur de démo)", example = "1")
        Long userId,

        @Schema(description = "Identifiant de l'anime du catalogue (obligatoire)", example = "1")
        @NotNull(message = "L'identifiant de l'anime est obligatoire")
        Long animeId,

        @Schema(description = "Statut initial de visionnage", example = "WATCHING")
        WatchStatus status,

        @Schema(description = "Épisode courant (≥ 0)", example = "3")
        @Min(value = 0, message = "Le numéro d'épisode ne peut pas être négatif")
        Integer currentEpisode,

        @Schema(description = "Note personnelle (0-10)", example = "8")
        @Min(value = 0, message = "La note minimale est 0")
        @Max(value = 10, message = "La note maximale est 10")
        Integer score
) {
}
