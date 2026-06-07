package fr.miage.numres.catalog;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

/*
DTO pour la mise à jour partielle d'un anime PATCH
Tous les champs sont optionnels : seuls ceux fournis (non nuls) sont appliqués
*/
@Schema(description = "Champs à mettre à jour partiellement sur un anime (tous optionnels)")
public record AnimePatchDTO(

        @Schema(description = "Nouveau titre", example = "Attack on Titan: Final Season")
        @Size(max = 255, message = "Le titre ne doit pas dépasser 255 caractères")
        String title,

        @Schema(description = "Nouveau synopsis")
        @Size(max = 2000, message = "Le synopsis ne doit pas dépasser 2000 caractères")
        String synopsis,

        @Schema(description = "Nouveau studio", example = "MAPPA")
        @Size(max = 255, message = "Le studio ne doit pas dépasser 255 caractères")
        String studio,

        @Schema(description = "Nouveau nombre d'épisodes (≥ 0)", example = "94")
        @Min(value = 0, message = "Le nombre d'épisodes ne peut pas être négatif")
        Integer episodes,

        @Schema(description = "Nouveaux genres", example = "Action,Drama")
        @Size(max = 255, message = "La liste des genres ne doit pas dépasser 255 caractères")
        String genres
) {
}
