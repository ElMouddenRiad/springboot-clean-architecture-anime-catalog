package fr.miage.numres.watchlist;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

/*
dto exposé par l'API pour une entrée de watchlist (lecture)
Enrichi d'informations issues du Catalogue animeTitle, totalEpisodes et de l'utilisateur code username
tout en gardant les entités Anime et User masquées
*/
@Schema(description = "Suivi d'un anime par un utilisateur")
public record WatchlistEntryDTO(

        @Schema(description = "Identifiant du suivi", example = "1")
        Long id,

        @Schema(description = "Identifiant du propriétaire", example = "1")
        Long userId,

        @Schema(description = "Nom de l'utilisateur propriétaire", example = "demo")
        String username,

        @Schema(description = "Identifiant de l'anime suivi", example = "1")
        Long animeId,

        @Schema(description = "Titre de l'anime (issu du catalogue)", example = "Attack on Titan")
        String animeTitle,

        @Schema(description = "Statut de visionnage", example = "WATCHING")
        WatchStatus status,

        @Schema(description = "Dernier épisode vu", example = "10")
        Integer currentEpisode,

        @Schema(description = "Nombre total d'épisodes (issu du catalogue)", example = "25")
        Integer totalEpisodes,

        @Schema(description = "Note personnelle (0-10)", example = "9")
        Integer score,

        @Schema(description = "Date de création du suivi")
        Instant createdAt,

        @Schema(description = "Date de dernière mise à jour")
        Instant updatedAt
) {
}
