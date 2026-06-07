package fr.miage.numres.watchlist;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

/*
Conversions pour le domaine Watchlist
Package-private
Le DTO de réponse est assemblé dans le service (il combine l'entité avec des données du Catalogue et de l'utilisateur) : 
il n'est donc pas produit ici
*/
@Mapper(componentModel = "spring")
interface WatchlistMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    WatchlistEntry toEntity(WatchlistEntryCreateDTO createDTO);

    // Remplacement complet (PUT) de l'état modifiable : statut, progression, score
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "animeId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void replaceEntityFromDto(WatchlistEntryReplaceDTO replaceDTO, @MappingTarget WatchlistEntry entry);

    // Mise à jour partielle (PATCH) : les champs nuls du DTO ne sont pas appliqués
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "animeId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void patchEntityFromDto(WatchlistEntryPatchDTO patchDTO, @MappingTarget WatchlistEntry entry);
}
