package fr.miage.numres.watchlist;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/*
Utilisateur propriétaire d'entrées de watchlist
Modèle volontairement minimal : pas d'authentification ni de métier (rôles, mot de passe, sécurité)
Package-private, interne au composant watchlis qui possède les suivis.
*/
@Entity
@Table(name = "app_user", uniqueConstraints = @UniqueConstraint(name = "uk_username", columnNames = "username"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String username;

    private String displayName;
}
