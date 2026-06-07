package fr.miage.numres.watchlist;

/*
Statut de visionnage d'un anime dans la watchlist d'un utilisateur
Public car exposé dans les DTOs et les paramètres d'API
*/
public enum WatchStatus {
    PLAN_TO_WATCH,
    WATCHING,
    COMPLETED,
    ON_HOLD,
    DROPPED
}
