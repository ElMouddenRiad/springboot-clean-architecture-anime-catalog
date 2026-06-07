package fr.miage.numres.catalog;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;

import java.util.List;

/*
Alimente le catalogue avec quelques animes de démonstration au démarrage
Désactivé pendant les tests (profil "test" 
Package-private
 */
@Configuration
@Profile("!test")
class CatalogDataInitializer {

    @Bean
    @Order(1)
    CommandLineRunner seedCatalogue(AnimeRepository repository) {
        return args -> {
            if (repository.count() > 0) {
                return;
            }
            repository.saveAll(List.of(
                    Anime.builder()
                            .title("Attack on Titan")
                            .synopsis("L'humanité combat des créatures géantes derrière des murs.")
                            .studio("Wit Studio")
                            .episodes(25)
                            .genres("Action,Drama,Fantasy")
                            .build(),
                    Anime.builder()
                            .title("Fullmetal Alchemist: Brotherhood")
                            .synopsis("Deux frères cherchent la pierre philosophale après un rituel raté.")
                            .studio("Bones")
                            .episodes(64)
                            .genres("Action,Adventure,Drama")
                            .build(),
                    Anime.builder()
                            .title("Steins;Gate")
                            .synopsis("Un groupe découvre comment envoyer des messages dans le passé.")
                            .studio("White Fox")
                            .episodes(24)
                            .genres("Sci-Fi,Thriller")
                            .build()
            ));
        };
    }
}
