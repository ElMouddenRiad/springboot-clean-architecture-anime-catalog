package fr.miage.numres.watchlist;

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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(WatchlistController.class)
class WatchlistControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private WatchlistService watchlistService;

    private WatchlistEntryDTO sampleDTO() {
        return new WatchlistEntryDTO(10L, 1L, "demo", 1L, "Attack on Titan", true,
                WatchStatus.WATCHING, 5, 25, 8, null, null);
    }

    @Test
    void getEntries_returnsOkWithList() throws Exception {
        when(watchlistService.getEntries(null, null)).thenReturn(List.of(sampleDTO()));

        mockMvc.perform(get("/api/watchlist"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(10))
                .andExpect(jsonPath("$[0].username").value("demo"))
                .andExpect(jsonPath("$[0].animeTitle").value("Attack on Titan"))
                .andExpect(jsonPath("$[0].animeAvailable").value(true))
                .andExpect(jsonPath("$[0].status").value("WATCHING"));
    }

    @Test
    void getEntries_filtersByUserAndStatus() throws Exception {
        when(watchlistService.getEntries(1L, WatchStatus.WATCHING)).thenReturn(List.of(sampleDTO()));

        mockMvc.perform(get("/api/watchlist").param("userId", "1").param("status", "WATCHING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userId").value(1));

        verify(watchlistService).getEntries(1L, WatchStatus.WATCHING);
    }

    @Test
    void getEntryById_whenMissing_returnsNotFound() throws Exception {
        when(watchlistService.getEntryById(99L)).thenThrow(new WatchlistEntryNotFoundException(99L));

        mockMvc.perform(get("/api/watchlist/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void addEntry_whenValid_returnsCreated() throws Exception {
        WatchlistEntryCreateDTO request = new WatchlistEntryCreateDTO(1L, 1L, WatchStatus.WATCHING, 5, 8);
        when(watchlistService.addEntry(any(WatchlistEntryCreateDTO.class))).thenReturn(sampleDTO());

        mockMvc.perform(post("/api/watchlist")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.totalEpisodes").value(25));
    }

    @Test
    void addEntry_whenAnimeIdMissing_returnsBadRequest() throws Exception {
        WatchlistEntryCreateDTO request = new WatchlistEntryCreateDTO(1L, null, WatchStatus.WATCHING, 5, 8);

        mockMvc.perform(post("/api/watchlist")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.animeId").exists());
    }

    @Test
    void replaceEntry_returnsOk() throws Exception {
        when(watchlistService.replaceEntry(eq(10L), any())).thenReturn(sampleDTO());

        mockMvc.perform(put("/api/watchlist/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"WATCHING\",\"currentEpisode\":5,\"score\":8}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10));
    }

    @Test
    void replaceEntry_whenMissingRequiredFields_returnsBadRequest() throws Exception {
        mockMvc.perform(put("/api/watchlist/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"score\":8}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.status").exists());
    }

    @Test
    void patchEntry_returnsOk() throws Exception {
        when(watchlistService.patchEntry(eq(10L), any())).thenReturn(sampleDTO());

        mockMvc.perform(patch("/api/watchlist/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"currentEpisode\":6}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10));
    }

    @Test
    void deleteEntry_returnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/watchlist/10"))
                .andExpect(status().isNoContent());

        verify(watchlistService).deleteEntry(10L);
    }
}
