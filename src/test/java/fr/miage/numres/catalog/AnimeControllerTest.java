package fr.miage.numres.catalog;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AnimeController.class)
class AnimeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private AnimeService animeService;

    private AnimeDTO sample() {
        return new AnimeDTO(1L, "Attack on Titan", "synopsis", "Wit Studio", 25, "Action", false);
    }

    @Test
    void getAllAnimes_returnsOkWithList() throws Exception {
        when(animeService.getAllAnimes()).thenReturn(List.of(sample()));

        mockMvc.perform(get("/api/catalog"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].title").value("Attack on Titan"));
    }

    @Test
    void getAnimeById_whenFound_returnsOk() throws Exception {
        when(animeService.getAnimeById(1L)).thenReturn(sample());

        mockMvc.perform(get("/api/catalog/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Attack on Titan"));
    }

    @Test
    void getAnimeById_whenMissing_returnsNotFound() throws Exception {
        when(animeService.getAnimeById(99L)).thenThrow(new AnimeNotFoundException(99L));

        mockMvc.perform(get("/api/catalog/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void createAnime_whenValid_returnsCreatedWithLocation() throws Exception {
        AnimeCreateDTO request = new AnimeCreateDTO("Bleach", "synopsis", "Pierrot", 366, "Action");
        when(animeService.createAnime(any(AnimeCreateDTO.class)))
                .thenReturn(new AnimeDTO(5L, "Bleach", "synopsis", "Pierrot", 366, "Action", false));

        mockMvc.perform(post("/api/catalog")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/catalog/5"))
                .andExpect(jsonPath("$.id").value(5));
    }

    @Test
    void createAnime_whenTitleBlank_returnsBadRequest() throws Exception {
        AnimeCreateDTO request = new AnimeCreateDTO("  ", "synopsis", "Pierrot", 366, "Action");

        mockMvc.perform(post("/api/catalog")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.title").exists());
    }

    @Test
    void replaceAnime_returnsOk() throws Exception {
        AnimeCreateDTO request = new AnimeCreateDTO("Attack on Titan", "new", "Wit Studio", 87, "Action");
        when(animeService.replaceAnime(eq(1L), any(AnimeCreateDTO.class)))
                .thenReturn(new AnimeDTO(1L, "Attack on Titan", "new", "Wit Studio", 87, "Action", false));

        mockMvc.perform(put("/api/catalog/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.episodes").value(87));
    }

    @Test
    void patchAnime_returnsOk() throws Exception {
        when(animeService.patchAnime(eq(1L), any()))
                .thenReturn(new AnimeDTO(1L, "Attack on Titan", "synopsis", "Wit Studio", 88, "Action", false));

        mockMvc.perform(patch("/api/catalog/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"episodes\":88}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.episodes").value(88));
    }

    @Test
    void deleteAnime_returnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/catalog/1"))
                .andExpect(status().isNoContent());

        verify(animeService).deleteAnime(1L);
    }

    @Test
    void deleteAnime_whenMissing_returnsNotFound() throws Exception {
        doThrow(new AnimeNotFoundException(99L)).when(animeService).deleteAnime(99L);

        mockMvc.perform(delete("/api/catalog/99"))
                .andExpect(status().isNotFound());
    }
}
