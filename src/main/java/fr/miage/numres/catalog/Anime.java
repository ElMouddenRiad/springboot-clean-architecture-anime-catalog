package fr.miage.numres.catalog;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "anime")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
class Anime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(length = 2000)
    private String synopsis;

    private String studio;

    private Integer episodes;

    private String genres;

    // Suppression logique (soft delete) : on désactive l'anime au lieu de le supprimer
    // physiquement afin de préserver l'intégrité référentielle vis-à-vis de la Watchlist
    @Column(name = "is_deleted", nullable = false)
    private boolean deleted;
}
