package fr.miage.numres.watchlist;

import fr.miage.numres.common.DuplicateResourceException;

/*
Exception lorsqu'on ajoute deux fois le même anime
Package-private : le handler global GlobalExceptionHandler intercepte le type de base (409)
*/
class DuplicateWatchlistEntryException extends DuplicateResourceException {

    DuplicateWatchlistEntryException(Long userId, Long animeId) {
        super("L'anime " + animeId + " est déjà présent dans la watchlist de l'utilisateur " + userId);
    }
}
