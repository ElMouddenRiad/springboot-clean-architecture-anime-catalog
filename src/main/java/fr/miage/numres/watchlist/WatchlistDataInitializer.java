package fr.miage.numres.watchlist;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;

import java.util.List;

/*
Crée des utilisateurs et des suivis de démonstration au démarrage
S'exécute après l'initialisation du catalogue, ordre afin que les animes référencés existent
Désactivé pendant les tests et Package-private
*/
@Configuration
@Profile("!test")
class WatchlistDataInitializer {

    @Bean
    @Order(2)
    CommandLineRunner seedUsers(UserRepository userRepository) {
        return args -> {
            if (userRepository.count() > 0) {
                return;
            }
            userRepository.saveAll(List.of(
                    User.builder().username("demo").displayName("Utilisateur Démo").build(),
                    User.builder().username("alice").displayName("Alice").build()
            ));
        };
    }

    @Bean
    @Order(3)
    CommandLineRunner seedWatchlist(WatchlistRepository repository, UserRepository userRepository) {
        return args -> {
            if (repository.count() > 0) {
                return;
            }
            Long demoId = userRepository.findByUsername("demo").orElseThrow().getId();

            repository.saveAll(List.of(
                    WatchlistEntry.builder()
                            .userId(demoId).animeId(1L)
                            .status(WatchStatus.WATCHING).currentEpisode(10).score(9)
                            .build(),
                    WatchlistEntry.builder()
                            .userId(demoId).animeId(2L)
                            .status(WatchStatus.COMPLETED).currentEpisode(64).score(10)
                            .build(),
                    WatchlistEntry.builder()
                            .userId(demoId).animeId(3L)
                            .status(WatchStatus.PLAN_TO_WATCH).currentEpisode(0)
                            .build()
            ));
        };
    }
}
