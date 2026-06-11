package fr.miage.numres.catalog;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/*
Implémentation package-private du metier
Reçoit des DTOs, les convertit en entités, délègue la persistance au
repository puis reconvertit en DTOs
-Dépendances injectées par constructeur
*/
@Service
@Transactional(readOnly = true)
class AnimeServiceImpl implements AnimeService {

    private final AnimeRepository repository;
    private final AnimeMapper mapper;

    AnimeServiceImpl(AnimeRepository repository, AnimeMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public List<AnimeDTO> getAllAnimes() {
        // Catalogue actif : on masque les animes supprimés logiquement
        return mapper.toDTOList(repository.findByDeletedFalse());
    }

    @Override
    public AnimeDTO getAnimeById(Long id) {
        // Vue "client" : un anime supprimé logiquement est considéré comme absent (404)
        return repository.findByIdAndDeletedFalse(id)
                .map(mapper::toDTO)
                .orElseThrow(() -> new AnimeNotFoundException(id));
    }

    @Override
    public Optional<AnimeDTO> findAnimeById(Long id) {
        // Recherche incluant les animes supprimés logiquement (pour enrichissement Watchlist)
        return repository.findById(id).map(mapper::toDTO);
    }

    @Override
    @Transactional
    public AnimeDTO createAnime(AnimeCreateDTO createDTO) {
        Anime entity = mapper.toEntity(createDTO);
        Anime saved = repository.save(entity);
        return mapper.toDTO(saved);
    }

    @Override
    @Transactional
    public AnimeDTO replaceAnime(Long id, AnimeCreateDTO updateDTO) {
        Anime entity = findAnimeOrThrow(id);
        mapper.updateEntityFromDto(updateDTO, entity);
        return mapper.toDTO(repository.save(entity));
    }

    @Override
    @Transactional
    public AnimeDTO patchAnime(Long id, AnimePatchDTO patchDTO) {
        Anime entity = findAnimeOrThrow(id);
        mapper.patchEntityFromDto(patchDTO, entity);
        return mapper.toDTO(repository.save(entity));
    }

    @Override
    @Transactional
    public void deleteAnime(Long id) {
        // Suppression LOGIQUE : on désactive l'anime au lieu de le retirer de la base.
        // Du point de vue de l'API il disparaît (absent de getAll, 404 sur getById),
        // mais la Watchlist peut toujours résoudre son titre (référence souple préservée).
        Anime anime = findAnimeOrThrow(id);
        anime.setDeleted(true);
        repository.save(anime);
    }

    // Recherche d'un anime ACTIF (les animes supprimés logiquement sont introuvables ici)
    private Anime findAnimeOrThrow(Long id) {
        return repository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new AnimeNotFoundException(id));
    }
}
