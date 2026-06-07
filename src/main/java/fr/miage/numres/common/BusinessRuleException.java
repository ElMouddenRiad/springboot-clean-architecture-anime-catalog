package fr.miage.numres.common;

/*
Exception lorsqu'une règle métier est violée 
(donnée cohérente syntaxiquement mais invalide fonctionnellement)
HTTP 400 Bad Request
*/
public class BusinessRuleException extends RuntimeException {

    public BusinessRuleException(String message) {
        super(message);
    }
}
