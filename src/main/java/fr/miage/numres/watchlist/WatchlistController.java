package fr.miage.numres.watchlist;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;

/*
Point d'entrée HTTP de la watchlist
Ne manipule que des DTOs et délègue toute la logique à WatchlistService
*/
@RestController
@RequestMapping("/api/watchlist")
@Tag(name = "Watchlist", description = "Suivi personnel des animes (progression, statut, score)")
public class WatchlistController {

    private final WatchlistService watchlistService;

    public WatchlistController(WatchlistService watchlistService) {
        this.watchlistService = watchlistService;
    }

    @GetMapping
    @Operation(summary = "Lister les suivis (filtres optionnels par utilisateur et statut)")
    public List<WatchlistEntryDTO> getEntries(@RequestParam(required = false) Long userId,
                                              @RequestParam(required = false) WatchStatus status) {
        return watchlistService.getEntries(userId, status);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Récupérer un suivi par son identifiant")
    public WatchlistEntryDTO getEntryById(@PathVariable Long id) {
        return watchlistService.getEntryById(id);
    }

    @PostMapping
    @Operation(summary = "Ajouter un anime à la watchlist (doit référencer un animeId valide)")
    public ResponseEntity<WatchlistEntryDTO> addEntry(@Valid @RequestBody WatchlistEntryCreateDTO createDTO,
                                                      UriComponentsBuilder uriBuilder) {
        WatchlistEntryDTO created = watchlistService.addEntry(createDTO);
        URI location = uriBuilder.path("/api/watchlist/{id}")
                .buildAndExpand(created.id())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Remplacer complètement l'état d'un suivi")
    public WatchlistEntryDTO replaceEntry(@PathVariable Long id,
                                          @Valid @RequestBody WatchlistEntryReplaceDTO replaceDTO) {
        return watchlistService.replaceEntry(id, replaceDTO);
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Mettre à jour partiellement un suivi (ex : progression)")
    public WatchlistEntryDTO patchEntry(@PathVariable Long id,
                                        @Valid @RequestBody WatchlistEntryPatchDTO patchDTO) {
        return watchlistService.patchEntry(id, patchDTO);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Retirer un suivi de la watchlist")
    public ResponseEntity<Void> deleteEntry(@PathVariable Long id) {
        watchlistService.deleteEntry(id);
        return ResponseEntity.noContent().build();
    }
}
