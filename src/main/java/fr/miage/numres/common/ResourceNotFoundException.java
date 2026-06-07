package fr.miage.numres.common;

/*
Exception de base lorsqu'une ressource n'existe pas (HTTP 404)
Publique car partagé par les domaines qui étendent 
( AnimeNotFoundException et WatchlistItemNotFoundException)
*/
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
