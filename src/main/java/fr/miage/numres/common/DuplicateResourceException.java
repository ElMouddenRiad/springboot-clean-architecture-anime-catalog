package fr.miage.numres.common;

/*
Exception d'une tentative de création d'une ressource déjà existante
Traduite en HTTP 409 (Conflict) géré par GlobalExceptionHandler 
*/
public class DuplicateResourceException extends RuntimeException {

    public DuplicateResourceException(String message) {
        super(message);
    }
}
