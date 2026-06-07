package fr.miage.numres.catalog;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
        return mapper.toDTOList(repository.findAll());
    }

    @Override
    public AnimeDTO getAnimeById(Long id) {
        return repository.findById(id)
                .map(mapper::toDTO)
                .orElseThrow(() -> new AnimeNotFoundException(id));
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
        if (!repository.existsById(id)) {
            throw new AnimeNotFoundException(id);
        }
        repository.deleteById(id);
    }

    private Anime findAnimeOrThrow(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new AnimeNotFoundException(id));
    }
}
