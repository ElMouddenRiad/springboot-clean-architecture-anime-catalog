package fr.miage.numres.catalog;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/*
Accès aux données des animes via JPA
Package-private utilisable uniquement par le service calatog

Note soft deleteles méthodes "ByDeletedFalse" ne renvoient que les animes
actifs (catalogue visible)
Le findById standard hérité de JpaRepository retrouve 
aussi les animes supprimés logiquement : c'est volontaire, pour permettre
à la Watchlist de résoudre le titre d'un anime retiré
*/
@Repository
interface AnimeRepository extends JpaRepository<Anime, Long> {

    // Catalogue actif uniquement (exclut les animes supprimés logiquement)
    List<Anime> findByDeletedFalse();

    Optional<Anime> findByIdAndDeletedFalse(Long id);
}
