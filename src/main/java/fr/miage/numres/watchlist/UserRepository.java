package fr.miage.numres.watchlist;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/*
Accès aux utilisateurs propriétaires de watchlists
Package-private: propre à watchlist
*/
@Repository
interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);
}
