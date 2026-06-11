package fr.miage.numres.catalog;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/*
Tests d'intégration bout-en-bout du Catalog
Contrairement aux WebMvcTest (service mocké), ces tests démarrent le
contexte Spring complet et traversent toute la pile :
Controller → Service → Repository → base H2
Le profil test désactive
les jeux de données de démonstration : la base démarre vide et déterministe
Transactional isole chaque test (rollback automatique)
*/
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class CatalogIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private long createAnime(String title, int episodes) throws Exception {
        String body = """
                { "title": "%s", "synopsis": "s", "studio": "Studio", "episodes": %d, "genres": "Action" }
                """.formatted(title, episodes);
        MvcResult result = mockMvc.perform(post("/api/catalog")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn();
        JsonNode node = objectMapper.readTree(result.getResponse().getContentAsString());
        return node.get("id").asLong();
    }

    @Test
    void createThenGetById_persistsThroughWholeStack() throws Exception {
        long id = createAnime("Cowboy Bebop", 26);

        mockMvc.perform(get("/api/catalog/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Cowboy Bebop"))
                .andExpect(jsonPath("$.episodes").value(26))
                .andExpect(jsonPath("$.deleted").value(false));
    }

    @Test
    void getById_whenMissing_returns404WithApiError() throws Exception {
        mockMvc.perform(get("/api/catalog/{id}", 999999))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void create_whenInvalidPayload_returns400WithFieldErrors() throws Exception {
        mockMvc.perform(post("/api/catalog")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"title\": \"\", \"episodes\": -5 }"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.title").exists())
                .andExpect(jsonPath("$.fieldErrors.episodes").exists());
    }

    @Test
    void delete_isLogical_animeDisappearsFromApiButGetReturns404() throws Exception {
        long id = createAnime("Death Note", 37);

        mockMvc.perform(delete("/api/catalog/{id}", id))
                .andExpect(status().isNoContent());

        // du point de vue de l'API l'anime est supprimé
        mockMvc.perform(get("/api/catalog/{id}", id))
                .andExpect(status().isNotFound());

        // et il n'apparaît plus dans la collection active
        mockMvc.perform(get("/api/catalog"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == %d)]".formatted(id)).isEmpty());
    }
}
