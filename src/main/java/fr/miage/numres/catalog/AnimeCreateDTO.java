package fr.miage.numres.catalog;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;


@Schema(description = "Données pour créer ou remplacer complètement un anime")
public record AnimeCreateDTO(

        @Schema(description = "Titre de l'anime (obligatoire)", example = "Attack on Titan")
        @NotBlank(message = "Le titre est obligatoire")
        @Size(max = 255, message = "Le titre ne doit pas dépasser 255 caractères")
        String title,

        @Schema(description = "Résumé de l'anime", example = "L'humanité combat des géants.")
        @Size(max = 2000, message = "Le synopsis ne doit pas dépasser 2000 caractères")
        String synopsis,

        @Schema(description = "Studio d'animation", example = "Wit Studio")
        @Size(max = 255, message = "Le studio ne doit pas dépasser 255 caractères")
        String studio,

        @Schema(description = "Nombre total d'épisodes (≥ 0)", example = "25")
        @Min(value = 0, message = "Le nombre d'épisodes ne peut pas être négatif")
        Integer episodes,

        @Schema(description = "Genres séparés par des virgules", example = "Action,Drama,Fantasy")
        @Size(max = 255, message = "La liste des genres ne doit pas dépasser 255 caractères")
        String genres
) {
}
