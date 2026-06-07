package fr.miage.numres.catalog;

import java.util.List;

/*
Abstraction publique de la logique métier du catalogue
cette interface respecte l'inversion des dépendances (D de SOLID)
*/
public interface AnimeService {

    List<AnimeDTO> getAllAnimes();

    AnimeDTO getAnimeById(Long id);

    AnimeDTO createAnime(AnimeCreateDTO createDTO);

    //Remplacement complet (PUT) d'un anime existant
    AnimeDTO replaceAnime(Long id, AnimeCreateDTO updateDTO);

    //Mise à jour partielle (PATCH) d'un anime existant
    AnimeDTO patchAnime(Long id, AnimePatchDTO patchDTO);

    void deleteAnime(Long id);
}
