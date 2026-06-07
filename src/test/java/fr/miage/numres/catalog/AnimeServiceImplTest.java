package fr.miage.numres.catalog;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnimeServiceImplTest {

    @Mock
    private AnimeRepository repository;

    @Mock
    private AnimeMapper mapper;

    @InjectMocks
    private AnimeServiceImpl service;

    @Test
    void getAllAnimes_returnsMappedDtos() {
        List<Anime> entities = List.of(new Anime(), new Anime());
        List<AnimeDTO> dtos = List.of(
                new AnimeDTO(1L, "A", null, null, null, null),
                new AnimeDTO(2L, "B", null, null, null, null)
        );
        when(repository.findAll()).thenReturn(entities);
        when(mapper.toDTOList(entities)).thenReturn(dtos);

        assertThat(service.getAllAnimes()).isEqualTo(dtos);
    }

    @Test
    void getAnimeById_whenFound_returnsDto() {
        Anime entity = Anime.builder().id(1L).title("Naruto").build();
        AnimeDTO dto = new AnimeDTO(1L, "Naruto", null, null, null, null);
        when(repository.findById(1L)).thenReturn(Optional.of(entity));
        when(mapper.toDTO(entity)).thenReturn(dto);

        assertThat(service.getAnimeById(1L)).isEqualTo(dto);
    }

    @Test
    void getAnimeById_whenMissing_throwsNotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getAnimeById(99L))
                .isInstanceOf(AnimeNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void createAnime_savesEntityAndReturnsDto() {
        AnimeCreateDTO createDTO = new AnimeCreateDTO("Bleach", "synopsis", "Pierrot", 366, "Action");
        Anime toSave = Anime.builder().title("Bleach").build();
        Anime saved = Anime.builder().id(5L).title("Bleach").build();
        AnimeDTO dto = new AnimeDTO(5L, "Bleach", "synopsis", "Pierrot", 366, "Action");

        when(mapper.toEntity(createDTO)).thenReturn(toSave);
        when(repository.save(toSave)).thenReturn(saved);
        when(mapper.toDTO(saved)).thenReturn(dto);

        AnimeDTO result = service.createAnime(createDTO);

        assertThat(result).isEqualTo(dto);
        verify(repository).save(any(Anime.class));
    }

    @Test
    void replaceAnime_whenFound_updatesAndReturnsDto() {
        AnimeCreateDTO dto = new AnimeCreateDTO("AoT", "s", "Wit", 87, "Action");
        Anime entity = Anime.builder().id(1L).title("AoT").build();
        AnimeDTO expected = new AnimeDTO(1L, "AoT", "s", "Wit", 87, "Action");
        when(repository.findById(1L)).thenReturn(Optional.of(entity));
        when(repository.save(entity)).thenReturn(entity);
        when(mapper.toDTO(entity)).thenReturn(expected);

        assertThat(service.replaceAnime(1L, dto)).isEqualTo(expected);
        verify(mapper).updateEntityFromDto(dto, entity);
    }

    @Test
    void replaceAnime_whenMissing_throwsNotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.replaceAnime(99L, new AnimeCreateDTO("x", null, null, null, null)))
                .isInstanceOf(AnimeNotFoundException.class);
    }

    @Test
    void patchAnime_whenFound_appliesPartialUpdate() {
        AnimePatchDTO patch = new AnimePatchDTO(null, null, null, 88, null);
        Anime entity = Anime.builder().id(1L).title("AoT").episodes(25).build();
        AnimeDTO expected = new AnimeDTO(1L, "AoT", null, null, 88, null);
        when(repository.findById(1L)).thenReturn(Optional.of(entity));
        when(repository.save(entity)).thenReturn(entity);
        when(mapper.toDTO(entity)).thenReturn(expected);

        assertThat(service.patchAnime(1L, patch)).isEqualTo(expected);
        verify(mapper).patchEntityFromDto(patch, entity);
    }

    @Test
    void deleteAnime_whenExists_deletes() {
        when(repository.existsById(1L)).thenReturn(true);

        service.deleteAnime(1L);

        verify(repository).deleteById(1L);
    }

    @Test
    void deleteAnime_whenMissing_throwsNotFound() {
        when(repository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> service.deleteAnime(99L))
                .isInstanceOf(AnimeNotFoundException.class);
        verify(repository, never()).deleteById(any());
    }
}
