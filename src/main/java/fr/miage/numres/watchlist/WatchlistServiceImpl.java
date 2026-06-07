package fr.miage.numres.watchlist;

import fr.miage.numres.catalog.AnimeDTO;
import fr.miage.numres.catalog.AnimeService;
import fr.miage.numres.common.BusinessRuleException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/*
Implémentation package-private de la logique métier de la watchlist
qui dépend de AnimeService (interface publique du Catalogue), et non du
repository ou de l'entité du Catalogue : 
l'interaction inter-domaines passe par une abstraction, ce qui préserve le découplage
et prépare la scission en microservices 
Chaque suivi est rattaché à un User validé
LesDependances injectées par constructeur
*/
@Service
@Transactional(readOnly = true)
class WatchlistServiceImpl implements WatchlistService {

    //Utilisateur appliqué par défaut tant que l'authentification n'existe pas
    private static final String DEFAULT_USERNAME = "demo";

    private final WatchlistRepository repository;
    private final WatchlistMapper mapper;
    private final AnimeService animeService;
    private final UserRepository userRepository;

    WatchlistServiceImpl(WatchlistRepository repository,
                         WatchlistMapper mapper,
                         AnimeService animeService,
                         UserRepository userRepository) {
        this.repository = repository;
        this.mapper = mapper;
        this.animeService = animeService;
        this.userRepository = userRepository;
    }

    @Override
    public List<WatchlistEntryDTO> getEntries(Long userId, WatchStatus status) {
        List<WatchlistEntry> entries;
        if (userId != null && status != null) {
            entries = repository.findByUserIdAndStatus(userId, status);
        } else if (userId != null) {
            entries = repository.findByUserId(userId);
        } else if (status != null) {
            entries = repository.findByStatus(status);
        } else {
            entries = repository.findAll();
        }
        return entries.stream().map(this::toDTO).toList();
    }

    @Override
    public WatchlistEntryDTO getEntryById(Long id) {
        return toDTO(findEntryOrThrow(id));
    }

    @Override
    @Transactional
    public WatchlistEntryDTO addEntry(WatchlistEntryCreateDTO createDTO) {
        // Résout et valide le propriétaire du suivi
        User owner = resolveOwner(createDTO.userId());

        // Vérifie l'existence de l'anime via le domaine Catalogue (404 si absent)
        AnimeDTO anime = animeService.getAnimeById(createDTO.animeId());

        if (repository.existsByUserIdAndAnimeId(owner.getId(), createDTO.animeId())) {
            throw new DuplicateWatchlistEntryException(owner.getId(), createDTO.animeId());
        }

        WatchlistEntry entry = mapper.toEntity(createDTO);
        entry.setUserId(owner.getId());
        if (entry.getStatus() == null) {
            entry.setStatus(WatchStatus.PLAN_TO_WATCH);
        }
        if (entry.getCurrentEpisode() == null) {
            entry.setCurrentEpisode(0);
        }

        applyBusinessRules(entry, anime);
        return toDTO(repository.save(entry), anime, owner.getUsername());
    }

    @Override
    @Transactional
    public WatchlistEntryDTO replaceEntry(Long id, WatchlistEntryReplaceDTO replaceDTO) {
        WatchlistEntry entry = findEntryOrThrow(id);
        mapper.replaceEntityFromDto(replaceDTO, entry);
        return saveWithRules(entry);
    }

    @Override
    @Transactional
    public WatchlistEntryDTO patchEntry(Long id, WatchlistEntryPatchDTO patchDTO) {
        WatchlistEntry entry = findEntryOrThrow(id);
        mapper.patchEntityFromDto(patchDTO, entry);
        return saveWithRules(entry);
    }

    @Override
    @Transactional
    public void deleteEntry(Long id) {
        repository.delete(findEntryOrThrow(id));
    }

    private WatchlistEntryDTO saveWithRules(WatchlistEntry entry) {
        AnimeDTO anime = animeService.getAnimeById(entry.getAnimeId());
        applyBusinessRules(entry, anime);
        // saveAndFlush force l'application de @UpdateTimestamp pour que la réponse
        // reflète immédiatement la date de mise à jour
        return toDTO(repository.saveAndFlush(entry), anime, usernameOf(entry.getUserId()));
    }

    private User resolveOwner(Long userId) {
        if (userId == null) {
            return userRepository.findByUsername(DEFAULT_USERNAME)
                    .orElseThrow(() -> new IllegalStateException(
                            "Utilisateur de démonstration '" + DEFAULT_USERNAME + "' introuvable"));
        }
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
    }

    private WatchlistEntry findEntryOrThrow(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new WatchlistEntryNotFoundException(id));
    }

    /*
    Règles métier appliquées avant persistance :
    la progression ne peut pas dépasser le nombre total d'épisodes
    le statut COMPLETED aligne automatiquement la progression sur le total
     */
    private void applyBusinessRules(WatchlistEntry entry, AnimeDTO anime) {
        Integer total = anime.episodes();
        if (entry.getStatus() == WatchStatus.COMPLETED && total != null) {
            entry.setCurrentEpisode(total);
            return;
        }
        if (total != null && entry.getCurrentEpisode() != null && entry.getCurrentEpisode() > total) {
            throw new BusinessRuleException(
                    "La progression (" + entry.getCurrentEpisode() + ") dépasse le nombre d'épisodes de l'anime ("
                            + total + ")");
        }
    }

    private String usernameOf(Long userId) {
        return userRepository.findById(userId).map(User::getUsername).orElse(null);
    }

    //Construit le DTO de réponse en récupérant les infos de l'anime et de l'utilisateur
    private WatchlistEntryDTO toDTO(WatchlistEntry entry) {
        AnimeDTO anime = animeService.getAnimeById(entry.getAnimeId());
        return toDTO(entry, anime, usernameOf(entry.getUserId()));
    }

    private WatchlistEntryDTO toDTO(WatchlistEntry entry, AnimeDTO anime, String username) {
        return new WatchlistEntryDTO(
                entry.getId(),
                entry.getUserId(),
                username,
                entry.getAnimeId(),
                anime.title(),
                entry.getStatus(),
                entry.getCurrentEpisode(),
                anime.episodes(),
                entry.getScore(),
                entry.getCreatedAt(),
                entry.getUpdatedAt()
        );
    }
}
