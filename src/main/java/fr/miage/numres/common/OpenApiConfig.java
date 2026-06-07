package fr.miage.numres.common;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/*
Métadonnées de la documentation OpenAPI / Swagger UI
Couvre les deux domaines exposés : Catalogue et Watchlist
*/
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI animeTrackerOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("AnimeTracker API")
                        .description("API REST d'AnimeTracker : Catalogue d'animes et Watchlist utilisateur "
                                + "(M2 MIAGE Numérique Responsable).")
                        .version("v1")
                        .contact(new Contact().name("AnimeTracker"))
                        .license(new License().name("Projet académique")));
    }
}
