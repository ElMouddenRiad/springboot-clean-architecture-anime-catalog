package fr.miage.numres.catalog;

import fr.miage.numres.common.ResourceNotFoundException;

// Levée lorsqu'aucun anime ne correspond à l'identifiant demandé
// Package-private : le handler global intercepte le type de base
/*  link ResourceNotFoundException HTTP 404, inutile de l'exposer
    publiquement */ 

class AnimeNotFoundException extends ResourceNotFoundException {

    AnimeNotFoundException(Long id) {
        super("Aucun anime trouvé pour l'identifiant " + id);
    }
}
