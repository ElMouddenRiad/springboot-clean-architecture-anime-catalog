package fr.miage.numres.catalog;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;


@Mapper(componentModel = "spring")
interface AnimeMapper {

    AnimeDTO toDTO(Anime anime);

    List<AnimeDTO> toDTOList(List<Anime> animes);

    // id et deleted gérés par le service jamais fournis par le client
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    Anime toEntity(AnimeCreateDTO createDTO);

    //Remplacement complet (PUT) : tous les champs sont écrasés, sauf l'identifiant et le flag deleted
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    void updateEntityFromDto(AnimeCreateDTO dto, @MappingTarget Anime entity);

    // Mise à jour partielle (PATCH) : les champs nuls du DTO ne sont pas appliqués
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    void patchEntityFromDto(AnimePatchDTO dto, @MappingTarget Anime entity);
}
