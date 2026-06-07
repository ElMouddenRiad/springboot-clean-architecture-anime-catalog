package fr.miage.numres.catalog;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/*
Accès aux données des animes via JPA
Package-private : utilisable uniquement par le service calatog
*/
@Repository
interface AnimeRepository extends JpaRepository<Anime, Long> {
}
