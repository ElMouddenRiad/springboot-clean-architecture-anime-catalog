package fr.miage.numres.watchlist;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/*
Contrat d'échange pour le remplacement complet d'un suivi (PUT)
-> l'intégralité de l'état modifiable est fournie 
Statut et progression sont obligatoires ; le score peut être omis (null = effacé)
Ne permet pas de changer l'utilisateur ni l'anime ciblés
*/
@Schema(description = "État complet d'un suivi pour remplacement (PUT)")
public record WatchlistEntryReplaceDTO(

        @Schema(description = "Statut de visionnage (obligatoire)", example = "COMPLETED")
        @NotNull(message = "Le statut est obligatoire")
        WatchStatus status,

        @Schema(description = "Épisode courant (obligatoire, ≥ 0)", example = "25")
        @NotNull(message = "La progression est obligatoire")
        @Min(value = 0, message = "Le numéro d'épisode ne peut pas être négatif")
        Integer currentEpisode,

        @Schema(description = "Note personnelle (0-10), optionnelle", example = "10")
        @Min(value = 0, message = "La note minimale est 0")
        @Max(value = 10, message = "La note maximale est 10")
        Integer score
) {
}
