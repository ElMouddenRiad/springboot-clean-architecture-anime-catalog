package fr.miage.numres;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/*
Point d'entrée de l'application AnimeTracker
Placé à la racine pour que le scan de composants
couvre les deux domaines : catalog (froid) et watchlist (chaud)
*/
@SpringBootApplication
public class SpringDemoNumres2526Application {

    public static void main(String[] args) {
        SpringApplication.run(SpringDemoNumres2526Application.class, args);
    }
}
