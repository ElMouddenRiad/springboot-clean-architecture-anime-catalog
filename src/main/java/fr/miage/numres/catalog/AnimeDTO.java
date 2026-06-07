package fr.miage.numres.catalog;

import io.swagger.v3.oas.annotations.media.Schema;


@Schema(description = "Représentation d'un anime du catalogue")
public record AnimeDTO(

        @Schema(description = "Identifiant unique", example = "1")
        Long id,

        @Schema(description = "Titre de l'anime", example = "Attack on Titan")
        String title,

        @Schema(description = "Résumé de l'anime", example = "L'humanité combat des géants.")
        String synopsis,

        @Schema(description = "Studio d'animation", example = "Wit Studio")
        String studio,

        @Schema(description = "Nombre total d'épisodes", example = "25")
        Integer episodes,

        @Schema(description = "Genres séparés par des virgules", example = "Action,Drama,Fantasy")
        String genres
) {
}
