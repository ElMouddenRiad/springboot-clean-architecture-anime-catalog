package fr.miage.numres.catalog;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;

/*
Point d'entrée HTTP de la watchlist
Ne manipule que des DTOs et délègue toute la logique à AnimeService
*/
@RestController
@RequestMapping("/api/catalog")
@Tag(name = "Catalogue", description = "Catalogue des animes (données officielles)")
public class AnimeController {

    private final AnimeService animeService;

    public AnimeController(AnimeService animeService) {
        this.animeService = animeService;
    }

    @GetMapping
    @Operation(summary = "Lister tous les animes du catalogue")
    public List<AnimeDTO> getAllAnimes() {
        return animeService.getAllAnimes();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Récupérer un anime par son identifiant")
    public AnimeDTO getAnimeById(@PathVariable Long id) {
        return animeService.getAnimeById(id);
    }

    @PostMapping
    @Operation(summary = "Créer un nouvel anime")
    public ResponseEntity<AnimeDTO> createAnime(@Valid @RequestBody AnimeCreateDTO createDTO,
                                                UriComponentsBuilder uriBuilder) {
        AnimeDTO created = animeService.createAnime(createDTO);
        URI location = uriBuilder.path("/api/catalog/{id}")
                .buildAndExpand(created.id())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Remplacer complètement un anime existant")
    public AnimeDTO replaceAnime(@PathVariable Long id, @Valid @RequestBody AnimeCreateDTO updateDTO) {
        return animeService.replaceAnime(id, updateDTO);
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Mettre à jour partiellement un anime (ex : nombre d'épisodes)")
    public AnimeDTO patchAnime(@PathVariable Long id, @Valid @RequestBody AnimePatchDTO patchDTO) {
        return animeService.patchAnime(id, patchDTO);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer un anime du catalogue")
    public ResponseEntity<Void> deleteAnime(@PathVariable Long id) {
        animeService.deleteAnime(id);
        return ResponseEntity.noContent().build();
    }
}
