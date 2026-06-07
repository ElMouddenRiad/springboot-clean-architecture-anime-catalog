package fr.miage.numres.watchlist;

import java.util.List;

/*
Abstraction publique de la logique métier de la watchlist (point d'accès du composant)
L'implémentation reste cachée dans le package.
*/
public interface WatchlistService {

    /*
    Liste les suivis, avec filtres optionnels
    userId filtre par propriétaire (peut être null)
    status filtre par statut de visionnage (peut être null)
    */
    List<WatchlistEntryDTO> getEntries(Long userId, WatchStatus status);

    WatchlistEntryDTO getEntryById(Long id);

    WatchlistEntryDTO addEntry(WatchlistEntryCreateDTO createDTO);

    // Remplacement complet (PUT) de l'état d'un suivi
    WatchlistEntryDTO replaceEntry(Long id, WatchlistEntryReplaceDTO replaceDTO);

    // Mise à jour partielle (PATCH) d'un suivi (par ex progression)
    WatchlistEntryDTO patchEntry(Long id, WatchlistEntryPatchDTO patchDTO);

    void deleteEntry(Long id);
}
