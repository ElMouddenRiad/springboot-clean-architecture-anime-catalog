package fr.miage.numres.watchlist;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

/*
Contrat d'échange pour la mise à jour partielle d'un suivi (PATCH),
typiquement pour faire progresser le nombre d'épisodes vus
Tous les champs sont optionnels : seuls ceux fournis (non nuls) sont appliqués
*/
@Schema(description = "Champs à mettre à jour partiellement sur un suivi (tous optionnels)")
public record WatchlistEntryPatchDTO(

        @Schema(description = "Nouveau statut", example = "COMPLETED")
        WatchStatus status,

        @Schema(description = "Nouvel épisode courant (≥ 0)", example = "12")
        @Min(value = 0, message = "Le numéro d'épisode ne peut pas être négatif")
        Integer currentEpisode,

        @Schema(description = "Nouvelle note (0-10)", example = "9")
        @Min(value = 0, message = "La note minimale est 0")
        @Max(value = 10, message = "La note maximale est 10")
        Integer score
) {
}
