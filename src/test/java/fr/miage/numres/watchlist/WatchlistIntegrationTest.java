package fr.miage.numres.watchlist;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/*
Tests d'intégration bout-en-bout de la Watchlist, y compris l'interaction
inter-domaines avec catalog (référence souple + suppression logique)
Contexte Spring complet, base H2, profil test (pas de seed),
Transactional pour l'isolation. Le test étant dans le package
watchlist, il peut préparer un utilisateur via le UserRepository
package-private — sans casser l'encapsulation côté production.
*/
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class WatchlistIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private Long userId;

    @BeforeEach
    void setUp() {
        userId = userRepository.save(
                User.builder().username("tester").displayName("Testeur").build()).getId();
    }

    private long createAnime(int episodes) throws Exception {
        String body = """
                { "title": "Attack on Titan", "synopsis": "s", "studio": "Wit", "episodes": %d, "genres": "Action" }
                """.formatted(episodes);
        MvcResult result = mockMvc.perform(post("/api/catalog")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
    }

    private long addEntry(long animeId, int currentEpisode) throws Exception {
        String body = """
                { "userId": %d, "animeId": %d, "status": "WATCHING", "currentEpisode": %d, "score": 7 }
                """.formatted(userId, animeId, currentEpisode);
        MvcResult result = mockMvc.perform(post("/api/watchlist")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
    }

    @Test
    void addThenPatchProgression_updatesThroughWholeStack() throws Exception {
        long animeId = createAnime(25);
        long entryId = addEntry(animeId, 5);

        // incrémenter la progression = envoyer la nouvelle valeur via PATCH
        mockMvc.perform(patch("/api/watchlist/{id}", entryId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"currentEpisode\": 6 }"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentEpisode").value(6))
                .andExpect(jsonPath("$.animeTitle").value("Attack on Titan"))
                .andExpect(jsonPath("$.animeAvailable").value(true));
    }

    @Test
    void addEntry_whenAnimeDoesNotExist_returns404() throws Exception {
        String body = """
                { "userId": %d, "animeId": 999999, "status": "WATCHING", "currentEpisode": 0 }
                """.formatted(userId);
        mockMvc.perform(post("/api/watchlist")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound());
    }

    @Test
    void addEntry_whenDuplicate_returns409() throws Exception {
        long animeId = createAnime(25);
        addEntry(animeId, 1);

        String body = """
                { "userId": %d, "animeId": %d, "status": "WATCHING", "currentEpisode": 2 }
                """.formatted(userId, animeId);
        mockMvc.perform(post("/api/watchlist")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict());
    }

    @Test
    void addEntry_whenAnimeIdMissing_returns400() throws Exception {
        String body = """
                { "userId": %d, "status": "WATCHING", "currentEpisode": 0 }
                """.formatted(userId);
        mockMvc.perform(post("/api/watchlist")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.animeId").exists());
    }

    @Test
    void addEntry_whenProgressExceedsEpisodes_returns400BusinessRule() throws Exception {
        long animeId = createAnime(12);
        String body = """
                { "userId": %d, "animeId": %d, "status": "WATCHING", "currentEpisode": 50 }
                """.formatted(userId, animeId);
        mockMvc.perform(post("/api/watchlist")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void softDeleteAnime_thenWatchlistEntryStaysReadableButUnavailable() throws Exception {
        long animeId = createAnime(25);
        long entryId = addEntry(animeId, 5);

        // admin retire l'anime du catalogue (suppression logique)
        mockMvc.perform(delete("/api/catalog/{id}", animeId))
                .andExpect(status().isNoContent());

        //suivi reste lisible : titre conservé mais signalé comme indisponible
        mockMvc.perform(get("/api/watchlist/{id}", entryId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.animeTitle").value("Attack on Titan"))
                .andExpect(jsonPath("$.animeAvailable").value(false));
    }
}
