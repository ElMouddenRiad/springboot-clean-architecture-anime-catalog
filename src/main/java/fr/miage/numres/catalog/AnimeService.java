package fr.miage.numres.catalog;

import java.util.List;
import java.util.Optional;

/*
Abstraction publique de la logique métier du catalogue
cette interface respecte l'inversion des dépendances (D de SOLID)
*/
public interface AnimeService {

    List<AnimeDTO> getAllAnimes();

    //Récupère un anime ou lève une 404 s'il n'existe pas
    AnimeDTO getAnimeById(Long id);

    //Recherche non-levante : enrichissement best-effort résilient (ex: Watchlist) si l'anime a été supprimé
    Optional<AnimeDTO> findAnimeById(Long id);

    AnimeDTO createAnime(AnimeCreateDTO createDTO);

    //Remplacement complet (PUT) d'un anime existant
    AnimeDTO replaceAnime(Long id, AnimeCreateDTO updateDTO);

    //Mise à jour partielle (PATCH) d'un anime existant
    AnimeDTO patchAnime(Long id, AnimePatchDTO patchDTO);

    void deleteAnime(Long id);
}
