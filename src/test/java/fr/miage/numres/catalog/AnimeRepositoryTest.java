package fr.miage.numres.catalog;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class AnimeRepositoryTest {

    @Autowired
    private AnimeRepository repository;

    @Test
    void saveAndFindById_persistsAnime() {
        Anime saved = repository.save(Anime.builder()
                .title("Cowboy Bebop")
                .studio("Sunrise")
                .episodes(26)
                .genres("Action,Sci-Fi")
                .build());

        assertThat(saved.getId()).isNotNull();
        assertThat(repository.findById(saved.getId()))
                .isPresent()
                .get()
                .extracting(Anime::getTitle)
                .isEqualTo("Cowboy Bebop");
    }
}
