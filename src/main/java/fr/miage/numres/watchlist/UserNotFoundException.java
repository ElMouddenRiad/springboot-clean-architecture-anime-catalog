package fr.miage.numres.watchlist;

import fr.miage.numres.common.ResourceNotFoundException;

/*
Eception lorsqu'aucun utilisateur ne correspond à l'identifiant fourni
Package-private : géré par le le handler global (404)
*/
class UserNotFoundException extends ResourceNotFoundException {

    UserNotFoundException(Long id) {
        super("Aucun utilisateur trouvé pour l'identifiant " + id);
    }
}
