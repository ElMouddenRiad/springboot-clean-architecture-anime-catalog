package fr.miage.numres.watchlist;

import fr.miage.numres.catalog.AnimeDTO;
import fr.miage.numres.catalog.AnimeService;
import fr.miage.numres.common.BusinessRuleException;
import fr.miage.numres.common.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WatchlistServiceImplTest {

    @Mock
    private WatchlistRepository repository;

    @Mock
    private WatchlistMapper mapper;

    @Mock
    private AnimeService animeService;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private WatchlistServiceImpl service;

    private AnimeDTO anime(int episodes) {
        return new AnimeDTO(1L, "Attack on Titan", "synopsis", "Wit Studio", episodes, "Action");
    }

    private User demoUser() {
        return User.builder().id(1L).username("demo").displayName("Démo").build();
    }

    @Test
    void addEntry_whenValid_enrichesWithAnimeAndUser() {
        WatchlistEntryCreateDTO dto = new WatchlistEntryCreateDTO(1L, 1L, WatchStatus.WATCHING, 5, 8);
        when(userRepository.findById(1L)).thenReturn(Optional.of(demoUser()));
        when(animeService.getAnimeById(1L)).thenReturn(anime(25));
        when(repository.existsByUserIdAndAnimeId(1L, 1L)).thenReturn(false);
        when(mapper.toEntity(dto)).thenReturn(WatchlistEntry.builder()
                .animeId(1L).status(WatchStatus.WATCHING).currentEpisode(5).score(8).build());
        when(repository.save(any(WatchlistEntry.class))).thenAnswer(inv -> {
            WatchlistEntry e = inv.getArgument(0);
            e.setId(100L);
            return e;
        });

        WatchlistEntryDTO result = service.addEntry(dto);

        assertThat(result.id()).isEqualTo(100L);
        assertThat(result.userId()).isEqualTo(1L);
        assertThat(result.username()).isEqualTo("demo");
        assertThat(result.animeTitle()).isEqualTo("Attack on Titan");
        assertThat(result.totalEpisodes()).isEqualTo(25);
        assertThat(result.currentEpisode()).isEqualTo(5);
    }

    @Test
    void addEntry_whenStatusCompleted_setsProgressToTotal() {
        WatchlistEntryCreateDTO dto = new WatchlistEntryCreateDTO(1L, 1L, WatchStatus.COMPLETED, 3, 10);
        when(userRepository.findById(1L)).thenReturn(Optional.of(demoUser()));
        when(animeService.getAnimeById(1L)).thenReturn(anime(25));
        when(repository.existsByUserIdAndAnimeId(1L, 1L)).thenReturn(false);
        when(mapper.toEntity(dto)).thenReturn(WatchlistEntry.builder()
                .animeId(1L).status(WatchStatus.COMPLETED).currentEpisode(3).score(10).build());
        when(repository.save(any(WatchlistEntry.class))).thenAnswer(inv -> inv.getArgument(0));

        WatchlistEntryDTO result = service.addEntry(dto);

        assertThat(result.currentEpisode()).isEqualTo(25);
    }

    @Test
    void addEntry_whenProgressExceedsTotal_throwsBusinessRule() {
        WatchlistEntryCreateDTO dto = new WatchlistEntryCreateDTO(1L, 1L, WatchStatus.WATCHING, 30, null);
        when(userRepository.findById(1L)).thenReturn(Optional.of(demoUser()));
        when(animeService.getAnimeById(1L)).thenReturn(anime(25));
        when(repository.existsByUserIdAndAnimeId(1L, 1L)).thenReturn(false);
        when(mapper.toEntity(dto)).thenReturn(WatchlistEntry.builder()
                .animeId(1L).status(WatchStatus.WATCHING).currentEpisode(30).build());

        assertThatThrownBy(() -> service.addEntry(dto))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void addEntry_whenAnimeMissing_propagatesNotFound() {
        WatchlistEntryCreateDTO dto = new WatchlistEntryCreateDTO(1L, 99L, WatchStatus.WATCHING, 0, null);
        when(userRepository.findById(1L)).thenReturn(Optional.of(demoUser()));
        when(animeService.getAnimeById(99L)).thenThrow(new ResourceNotFoundException("anime 99 introuvable"));

        assertThatThrownBy(() -> service.addEntry(dto))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void addEntry_whenUserMissing_throwsUserNotFound() {
        WatchlistEntryCreateDTO dto = new WatchlistEntryCreateDTO(42L, 1L, WatchStatus.WATCHING, 0, null);
        when(userRepository.findById(42L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.addEntry(dto))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void addEntry_whenDuplicate_throwsConflict() {
        WatchlistEntryCreateDTO dto = new WatchlistEntryCreateDTO(1L, 1L, WatchStatus.WATCHING, 0, null);
        when(userRepository.findById(1L)).thenReturn(Optional.of(demoUser()));
        when(animeService.getAnimeById(1L)).thenReturn(anime(25));
        when(repository.existsByUserIdAndAnimeId(1L, 1L)).thenReturn(true);

        assertThatThrownBy(() -> service.addEntry(dto))
                .isInstanceOf(DuplicateWatchlistEntryException.class);
    }

    @Test
    void replaceEntry_whenFound_appliesAndReturnsDto() {
        WatchlistEntryReplaceDTO dto = new WatchlistEntryReplaceDTO(WatchStatus.WATCHING, 5, 7);
        WatchlistEntry entry = WatchlistEntry.builder()
                .id(10L).userId(1L).animeId(1L).status(WatchStatus.WATCHING).currentEpisode(5).build();
        when(repository.findById(10L)).thenReturn(Optional.of(entry));
        when(animeService.getAnimeById(1L)).thenReturn(anime(25));
        when(repository.saveAndFlush(entry)).thenReturn(entry);
        when(userRepository.findById(1L)).thenReturn(Optional.of(demoUser()));

        WatchlistEntryDTO result = service.replaceEntry(10L, dto);

        assertThat(result.id()).isEqualTo(10L);
        assertThat(result.username()).isEqualTo("demo");
        verify(mapper).replaceEntityFromDto(dto, entry);
    }

    @Test
    void patchEntry_whenFound_appliesPartialUpdate() {
        WatchlistEntryPatchDTO dto = new WatchlistEntryPatchDTO(null, 6, null);
        WatchlistEntry entry = WatchlistEntry.builder()
                .id(10L).userId(1L).animeId(1L).status(WatchStatus.WATCHING).currentEpisode(6).build();
        when(repository.findById(10L)).thenReturn(Optional.of(entry));
        when(animeService.getAnimeById(1L)).thenReturn(anime(25));
        when(repository.saveAndFlush(entry)).thenReturn(entry);
        when(userRepository.findById(1L)).thenReturn(Optional.of(demoUser()));

        WatchlistEntryDTO result = service.patchEntry(10L, dto);

        assertThat(result.currentEpisode()).isEqualTo(6);
        verify(mapper).patchEntityFromDto(dto, entry);
    }

    @Test
    void getEntryById_whenMissing_throwsNotFound() {
        when(repository.findById(42L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getEntryById(42L))
                .isInstanceOf(WatchlistEntryNotFoundException.class);
    }

    @Test
    void deleteEntry_whenMissing_throwsNotFound() {
        when(repository.findById(42L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.deleteEntry(42L))
                .isInstanceOf(WatchlistEntryNotFoundException.class);
    }
}
