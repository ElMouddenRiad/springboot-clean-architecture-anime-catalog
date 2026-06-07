package fr.miage.numres.watchlist;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/*
Accès aux données des entrées de watchlist via JPA
Package-private : propore à watchlist
*/
@Repository
interface WatchlistRepository extends JpaRepository<WatchlistEntry, Long> {

    List<WatchlistEntry> findByUserId(Long userId);

    List<WatchlistEntry> findByUserIdAndStatus(Long userId, WatchStatus status);

    List<WatchlistEntry> findByStatus(WatchStatus status);

    boolean existsByUserIdAndAnimeId(Long userId, Long animeId);
}
