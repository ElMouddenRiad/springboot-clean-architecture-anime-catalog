# AnimeTracker - Backend

Backend Spring Boot du projet **AnimeTracker**, organisé autour de **deux domaines découplés** :

- **Catalogue** : base de connaissance « froide » (informations officielles des animes : titre, studio, épisodes, genres).
- **Watchlist** : base « chaude » et dynamique liée à l'activité de l'utilisateur (statut de visionnage, progression par épisode, score personnel).

La Watchlist référence un anime **par son identifiant** (référence souple) et vérifie son existence via l'**interface** `AnimeService` : les deux domaines restent ainsi indépendants et pourront évoluer vers des microservices séparés.

-La suppression d'un anime est une **suppression logique (soft delete)** : l'anime disparaît du catalogue côté API, mais reste résoluble par la Watchlist (le titre est conservé, signalé `animeAvailable: false`). Voir [Référence souple & suppression logique](#référence-souple--suppression-logique-soft-delete).

## Contexte académique
Projet réalisé dans le cadre du **M2 MIAGE Numérique Responsable**.

## Documentation
- [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md) - choix d'architecture (Package by Component, soft delete, référence souple).
- [`docs/TESTS.md`](docs/TESTS.md) - stratégie de tests (unitaires, slice, persistance, intégration) et interprétation des 53 tests.
- [`docs/DTOS.md`](docs/DTOS.md) - rôle des DTOs, champs obligatoires et workflow entrée→traitement→sortie.
- [`docs/EXCEPTIONS.md`](docs/EXCEPTIONS.md) - gestion des erreurs (codes, messages, `ApiError`), absence de 500, et champs POST/PUT/PATCH.

## English summary
This Spring Boot project focuses on the **AnimeTracker Catalogue Backend**.
It provides a clean backend for the catalog of anime titles, separated from the future user watchlist.

Main goals:
- strict layer separation,
- **SOLID** principles,
- encapsulation of JPA entities,
- API exposure through DTOs only.

## Objectif
Construire une architecture backend propre, testable et évolutive en respectant :
- la séparation stricte des couches,
- les principes **SOLID**,
- l'encapsulation des entités JPA,
- une exposition API uniquement via des DTOs.

## Objective
Build a clean, testable and evolutive backend while respecting:
- strict layer separation,
- **SOLID** principles,
- encapsulation of JPA entities,
- API exposure through DTOs only.

## Stack technique
- Java 25 (toolchain Gradle)
- Spring Boot 4.0.2
- Spring Data JPA
- H2 en mémoire
- Spring Web MVC
- Bean Validation (Jakarta)
- MapStruct (mapping entité ↔ DTO)
- Springdoc OpenAPI / Swagger UI
- Lombok
- Gradle

## Technical stack
- Java 25 (Gradle toolchain)
- Spring Boot 4.0.2
- Spring Data JPA
- H2 in-memory database
- Spring Web MVC
- Bean Validation (Jakarta)
- MapStruct (entity ↔ DTO mapping)
- Springdoc OpenAPI / Swagger UI
- Lombok
- Gradle

## Architecture cible
Le projet est organisé en **Package by Component** : un package par domaine
(`catalog`, `watchlist`), chacun regroupant les rôles suivants :

- **API / Controllers** : point d'entrée HTTP, manipulation de DTOs uniquement
- **Service** : logique métier, orchestration, conversion DTO ↔ entité (interface publique + implémentation cachée)
- **Repository** : accès aux données via Spring Data JPA (package-private)
- **Entity** : modèle de persistance interne au composant (package-private)
- **DTOs** : contrats d'échange exposés à l'extérieur (public)
- **Mapper** : conversion entre entités et DTOs (package-private)

## Target architecture
The project is organized by **feature** around the catalogue:

- **API / Controllers**: HTTP entry point, DTO-only handling
- **Service**: business logic, orchestration, DTO ↔ entity conversion
- **Repository**: data access through Spring Data JPA
- **Entity**: internal persistence model
- **DTOs**: external data contracts
- **Mapper**: entity and DTO conversion

### Principe important
Aucune entité JPA ne doit être exposée directement par l'API.

### Important principle
No JPA entity should be exposed directly by the API.

## Structure des packages (Package by Component)
Chaque domaine est un **package unique et plat** (pas de sous-packages par couche),
ce qui permet d'utiliser la **visibilité Java** pour encapsuler les détails :

```
fr.miage.numres
├── common        # noyau transversal : ApiError, exceptions de base, GlobalExceptionHandler, OpenApiConfig
├── catalog       # composant Catalogue : Anime(+DTOs), AnimeRepository, AnimeMapper,
│                 #   AnimeService (public) + AnimeServiceImpl (caché), AnimeController, exception, seeder
└── watchlist     # composant Watchlist : WatchlistEntry/User(+DTOs), repositories, mapper,
                  #   WatchlistService (public) + WatchlistServiceImpl (caché), WatchlistController, exceptions, seeder
```

**Encapsulation par visibilité :**
- `public` : interfaces de service (`AnimeService`, `WatchlistService`), DTOs, enum `WatchStatus`, contrôleurs ;
- `package-private` : implémentations (`*ServiceImpl`), repositories, entités JPA, mappers et exceptions de domaine.

> **Pourquoi un package plat ?** Le *package-private* de Java n'est visible que dans le **même package**. Pour que l'implémentation cachée puisse utiliser un repository caché, les deux doivent cohabiter dans un package unique. Garder des sous-dossiers par couche (`controllers/`, `services/`…) obligerait à tout rendre `public` - c'est-à-dire à retomber sur le *Package by Layer*, l'approche la plus faible en encapsulation selon le cours (Chapitre 3). L'aplatissement est donc la condition nécessaire pour appliquer un vrai **Package by Component**. Voir [`docs/ARCHITECTURE.md` §4.0](docs/ARCHITECTURE.md) pour la justification détaillée.

La classe `@SpringBootApplication` est à la racine `fr.miage.numres` afin de scanner les deux composants.

### Gestion de l'utilisateur
Chaque suivi de watchlist appartient à un `User` (entité du module watchlist). En l'absence
d'authentification (hors périmètre), un utilisateur de démonstration `demo` est utilisé par
défaut si aucun `userId` n'est fourni ; un `userId` explicite est validé (404 s'il n'existe pas).
La sécurité et le métier utilisateur complets ne sont volontairement pas implémentés.

## Fonctionnalités du catalogue
- lister, consulter, créer, **remplacer (PUT)**, **modifier partiellement (PATCH)** et **supprimer (suppression logique)** un anime

## Référence souple & suppression logique (soft delete)
Les deux domaines ne partagent **aucune clé étrangère** : la Watchlist ne connaît qu'un `animeId`
et appelle l'interface `AnimeService` pour enrichir ses réponses (titre, nombre d'épisodes). Ce
couplage faible prépare une future séparation en microservices.

Pour que cette référence reste cohérente même quand un anime est retiré, le Catalogue applique une
**suppression logique** :

- `DELETE /api/catalog/{id}` ne supprime pas la ligne en base : il positionne un drapeau `deleted = true`.
- Côté **API publique**, l'anime est considéré comme absent : il disparaît de `GET /api/catalog` et
  `GET /api/catalog/{id}` renvoie `404`. Le contrat REST est donc identique à une vraie suppression.
- Côté **Watchlist**, l'anime reste **résoluble en interne** : les suivis existants conservent le titre
  réel et sont marqués `animeAvailable: false` (au lieu de planter ou de perdre l'information).
- On **ne peut plus ajouter** un anime supprimé à une watchlist (`POST /api/watchlist` → `404`), mais on
  peut **continuer à mettre à jour** un suivi déjà existant.

Choix d'implémentation : pas d'annotation `@SQLRestriction`/`@Where` globale (qui masquerait l'anime
*partout*, y compris pour la Watchlist). Le filtrage est explicite via des requêtes dédiées
(`findByDeletedFalse`, `findByIdAndDeletedFalse`), tandis que `findById` reste non filtré pour
l'enrichissement. On combine ainsi **soft delete** et **résilience**.

## Fonctionnalités de la watchlist
- ajouter un anime à sa watchlist (avec vérification de son existence dans le catalogue et de l'utilisateur)
- lister les suivis, avec filtres optionnels par utilisateur et par statut
- récupérer un suivi par identifiant
- remplacer complètement (PUT) ou mettre à jour partiellement (PATCH) un suivi
- retirer un suivi

### Règles métier de la watchlist
- la progression ne peut pas dépasser le nombre d'épisodes de l'anime (sinon `400`),
- un anime déjà présent dans la watchlist d'un utilisateur ne peut pas être ajouté deux fois (sinon `409`),
- passer au statut `COMPLETED` aligne automatiquement la progression sur le nombre total d'épisodes,
- statuts possibles : `PLAN_TO_WATCH`, `WATCHING`, `COMPLETED`, `ON_HOLD`, `DROPPED`.

## Endpoints REST
### Catalogue - préfixe `/api/catalog`
| Verbe | Endpoint | Action |
|-------|----------|--------|
| GET | `/api/catalog` | Lister tous les animes |
| POST | `/api/catalog` | Créer un anime (validation) |
| GET | `/api/catalog/{id}` | Détails d'un anime |
| PUT | `/api/catalog/{id}` | Remplacement complet |
| PATCH | `/api/catalog/{id}` | Mise à jour partielle |
| DELETE | `/api/catalog/{id}` | Supprimer un anime (**suppression logique**, renvoie `204`) |

### Watchlist - préfixe `/api/watchlist`
| Verbe | Endpoint | Action |
|-------|----------|--------|
| GET | `/api/watchlist` | Lister les suivis (filtres optionnels `?userId=` et `?status=`) |
| POST | `/api/watchlist` | Ajouter un anime à la liste (référence un `animeId` valide) |
| GET | `/api/watchlist/{id}` | Détails d'un suivi |
| PUT | `/api/watchlist/{id}` | Remplacer l'état complet du suivi |
| PATCH | `/api/watchlist/{id}` | Mise à jour de la progression |
| DELETE | `/api/watchlist/{id}` | Retirer le suivi |

## Documentation interactive
Swagger UI est disponible via :

- `http://localhost:8080/swagger-ui.html`
- ou `http://localhost:8080/swagger-ui/index.html`

La spec OpenAPI est exposée sur :
- `http://localhost:8080/v3/api-docs`

## Base de données
Le projet utilise **H2 en mémoire**.

Console H2 :
- `http://localhost:8080/h2-console`

Paramètre JDBC utilisé dans la configuration actuelle :
- `jdbc:h2:mem:devDb`

## Lancement du projet
### Compilation
```bash
./gradlew.bat build
```

### Démarrage
```bash
./gradlew.bat bootRun
```

## Test rapide via Swagger
1. Démarrer l'application
2. Ouvrir Swagger UI
3. Tester un `POST /api/catalog`
4. Relancer un `GET /api/catalog`
5. Tester un `GET /api/catalog/{id}`

### Exemple de payload - création d'un anime (`POST /api/catalog`)
```json
{
  "title": "Attack on Titan",
  "synopsis": "Humanity fights giant creatures behind walls.",
  "studio": "Wit Studio",
  "episodes": 25,
  "genres": "Action,Drama,Fantasy"
}
```

### Exemple de payload - ajout à la watchlist (`POST /api/watchlist`)
```json
{
  "userId": 1,
  "animeId": 1,
  "status": "WATCHING",
  "currentEpisode": 3,
  "score": 8
}
```

### Exemple de payload - remplacement complet d'un suivi (`PUT /api/watchlist/{id}`)
```json
{
  "status": "COMPLETED",
  "currentEpisode": 25,
  "score": 10
}
```

### Exemple de payload - mise à jour partielle d'un suivi (`PATCH /api/watchlist/{id}`)
```json
{
  "currentEpisode": 12
}
```

## Données de démonstration
Au démarrage (hors profil `test`), le catalogue est pré-rempli avec 3 animes, deux utilisateurs (`demo`, `alice`) sont créés et une watchlist de démonstration est associée à l'utilisateur `demo`.

## Tests
```bash
./gradlew.bat test
```
Couverture (53 tests) :
- **tests unitaires** des services avec Mockito (`AnimeServiceImplTest`, `WatchlistServiceImplTest`) - règles métier, soft delete, référence souple ;
- **tests de tranche** des contrôleurs (`@WebMvcTest` : `AnimeControllerTest`, `WatchlistControllerTest`) - sérialisation JSON, codes HTTP, validation ;
- **test de persistance** (`@DataJpaTest` : `AnimeRepositoryTest`) ;
- **tests d'intégration bout-en-bout** (`@SpringBootTest` + `MockMvc` : `CatalogIntegrationTest`, `WatchlistIntegrationTest`) - toute la pile Controller → Service → Repository → H2, dont le scénario inter-domaines « soft delete d'un anime suivi » ;
- **chargement du contexte** Spring (`SpringDemoNumres2526ApplicationTests`).

Le rapport HTML est généré dans `build/reports/tests/test/index.html`.

## État actuel du projet
Phases 1 et 2 **complètes et fonctionnelles** :
- les deux domaines (Catalogue et Watchlist) sont implémentés avec une séparation stricte des couches,
- API REST complète sur les deux modules : `GET`, `POST`, `GET/{id}`, `PUT`, `PATCH`, `DELETE`,
- aucune entité JPA n'est exposée : l'API n'échange que des DTOs (entrée et sortie),
- gestion de l'utilisateur côté watchlist (sans sécurité/authentification),
- gestion d'erreurs normalisée (404 / 409 / 400) et validation des entrées (`@NotNull`, `@Min`, `@Size`…),
- **suppression logique (soft delete)** du catalogue + **référence souple résiliente** côté watchlist,
- documentation Swagger fonctionnelle pour tester chaque endpoint,
- tests automatisés au vert : unitaires, tranche, persistance **et intégration bout-en-bout**.

Évolutions possibles (phases suivantes) :
- authentification et métier utilisateur réels,
- séparation physique en microservices indépendants (1 base par service + événements de synchronisation)

## Valeur pédagogique
Ce projet illustre :
- l'injection de dépendances,
- la séparation des responsabilités,
- la construction d'un domaine métier simple mais évolutif,
- la mise à disposition d'une API testable avec Swagger.

## Licence
Projet académique réalisé dans le cadre d'un TP Spring Boot.
