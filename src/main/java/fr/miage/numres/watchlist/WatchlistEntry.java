package fr.miage.numres.watchlist;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

/*
Suivi par un utilisateur d'un anime du catalogue (donnée chaude)
Le lien vers le catalogue se fait par animeId,
jamais une relation ManyToOne vers Anime : c'est la clé du découplage qui permettra de scinder les domaines en microservices
-> passage microservices facilité
Package-private et annotée Getter/Setter lombok (pas Data : mauvaise pratique)
*/
@Entity
@Table(
        name = "watchlist_entry",
        uniqueConstraints = @UniqueConstraint(name = "uk_user_anime", columnNames = {"user_id", "anime_id"}),
        indexes = @Index(name = "idx_watchlist_user", columnList = "user_id")
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
class WatchlistEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Propriétaire de l'entrée (référence souple vers User)
    @Column(name = "user_id", nullable = false)
    private Long userId;

    // Référence souple vers l'anime du catalogue
    @Column(name = "anime_id", nullable = false)
    private Long animeId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private WatchStatus status;

    // Dernier épisode visionné
    @Column(nullable = false)
    private Integer currentEpisode;

    // Note personnelle de l'utilisateur (0 à 10) optionnelle
    private Integer score;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;
}
