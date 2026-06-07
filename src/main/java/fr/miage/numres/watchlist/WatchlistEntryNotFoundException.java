package fr.miage.numres.watchlist;

import fr.miage.numres.common.ResourceNotFoundException;

/*
Exception lorsqu'aucun suivi ne correspond à l'identifiant demandé
Package-private : le handler global intercepte le type de base (404)
*/
class WatchlistEntryNotFoundException extends ResourceNotFoundException {

    WatchlistEntryNotFoundException(Long id) {
        super("Aucun suivi trouvé pour l'identifiant " + id);
    }
}
