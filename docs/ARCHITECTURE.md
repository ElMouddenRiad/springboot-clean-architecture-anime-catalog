# Architecture du projet AnimeTracker

## 1. Contexte
AnimeTracker est un backend Spring Boot conçu pour servir de socle à une plateforme de suivi d'animes.
Le projet est volontairement découpé en deux univers :

- **Catalogue** : données stables et officielles sur les animes.
- **Watchlist** : données dynamiques liées à l'utilisateur, prévues pour une phase ultérieure.

Cette séparation permet d'anticiper une future évolution vers des services indépendants.

## 2. Objectifs d'architecture
L'architecture vise à respecter :

- **Single Responsibility Principle** : chaque classe a un rôle unique.
- **Open/Closed Principle** : extension sans modification massive.
- **Dependency Inversion Principle** : les dépendances sont injectées via abstractions.
- **Encapsulation** : les entités JPA restent internes au backend.
- **Testabilité** : la logique métier est isolée des détails de persistance.

## 3. Découpage des couches

### 3.1 Couche API
Responsable des échanges HTTP.

Rôle :
- recevoir les requêtes,
- valider les entrées,
- manipuler uniquement des DTOs,
- retourner des réponses HTTP adaptées.

Bonnes pratiques :
- pas d'accès direct à la base,
- pas d'entité JPA exposée,
- logique métier minimale.

### 3.2 Couche Service
Responsable de la logique métier.

Rôle :
- orchestrer les opérations,
- appliquer les règles métier,
- gérer les conversions entre entités et DTOs,
- centraliser la logique du catalogue.

### 3.3 Couche Data / Repository
Responsable de l'accès aux données.

Rôle :
- interroger H2 via Spring Data JPA,
- fournir les opérations CRUD,
- rester minimaliste.

### 3.4 Couche Entity
Responsable du modèle de persistance.

Rôle :
- représenter les données stockées,
- rester invisible à l'extérieur du service.

### 3.5 Couche DTO
Responsable des contrats d'échange.

Rôle :
- sécuriser l'API,
- stabiliser les payloads,
- découpler l'API du modèle JPA.

## 4. Packages (Package by Component)
Le code est organisé **par composant** : chaque domaine est un **package unique et
plat** (pas de sous-packages par couche du type `controllers`, `services`,
`repositories`). Cela permet de s'appuyer sur la **visibilité Java** pour encapsuler
les détails d'implémentation, et prépare une extraction propre en microservices.

```
fr.miage.numres
├── SpringDemoNumres2526Application   (@SpringBootApplication, à la racine)
├── common                            (noyau transversal, partagé sans coupler les domaines)
│   ├── ApiError                      (public) — payload d'erreur normalisé
│   ├── ResourceNotFoundException     (public) — base 404
│   ├── DuplicateResourceException    (public) — base 409
│   ├── BusinessRuleException         (public) — base 400
│   ├── GlobalExceptionHandler        (@RestControllerAdvice)
│   └── OpenApiConfig                 (config Swagger/OpenAPI)
├── catalog                           (composant Catalogue — package unique)
│   ├── AnimeController               (public)
│   ├── AnimeService                  (public)        ← interface
│   ├── AnimeServiceImpl              (package-private) ← implémentation cachée
│   ├── AnimeRepository               (package-private)
│   ├── Anime                         (package-private) ← entité JPA, jamais exposée
│   ├── AnimeDTO / AnimeCreateDTO / AnimePatchDTO  (public) ← contrats d'API
│   ├── AnimeMapper                   (package-private)
│   ├── AnimeNotFoundException        (package-private, extends ResourceNotFoundException)
│   └── CatalogDataInitializer        (package-private)
└── watchlist                         (composant Watchlist — package unique)
    ├── WatchlistController           (public)
    ├── WatchlistService              (public)        ← interface
    ├── WatchlistServiceImpl          (package-private) ← implémentation cachée
    ├── WatchlistRepository / UserRepository           (package-private)
    ├── WatchlistEntry / User         (package-private) ← entités JPA
    ├── WatchStatus                   (public enum)
    ├── WatchlistEntry*DTO            (public) ← Create / Replace / Patch / réponse
    ├── WatchlistMapper               (package-private)
    ├── *NotFoundException / DuplicateWatchlistEntryException (package-private)
    └── WatchlistDataInitializer      (package-private)
```

**Règle de visibilité appliquée :**
- **public** : interfaces de service (`AnimeService`, `WatchlistService`), DTOs,
  enum exposé (`WatchStatus`), contrôleurs, et le noyau `common`.
- **package-private** : implémentations de service (`*ServiceImpl`), repositories,
  entités JPA, mappers et exceptions spécifiques au domaine.

Comme tout le composant vit dans un seul package, l'implémentation peut utiliser un
repository package-private sans jamais l'exposer à l'extérieur. La seule porte
d'entrée d'un domaine est donc son interface de service (+ ses DTOs).

La classe `@SpringBootApplication` est placée à la racine `fr.miage.numres` pour
scanner les deux composants.

### 4.0 Pourquoi un package plat ? (justification du choix)
Le cours (Chapitre 3 — *Structure du code et Encapsulation*) compare quatre
approches, classées par niveau d'encapsulation croissant :

| Approche | Organisation | Encapsulation | Notre verdict |
|----------|--------------|---------------|---------------|
| **Package by Layer** | par rôle technique (`web`, `service`, `data`) | **Faible** : tout doit être `public` | ❌ Interdit par le sujet |
| **Package by Feature** | par domaine métier (un dossier = une feature) | **Meilleure** : permet le package-private | ➖ Point de départ |
| **Package by Component** | feature + **isolation forcée** (boîte noire, une seule interface exposée) | **Forte** | ✅ **Choisi** |
| **Ports & Adapters (Hexagonal)** | domaine totalement isolé de l'infra via des ports | **Maximale** | ➖ Surdimensionné pour ce TP |

Nous avons retenu le **Package by Component**, présenté par le cours comme
« l'équilibre parfait entre les couches et les fonctionnalités ». Le composant agit
comme une **boîte noire** : il n'expose qu'une **interface de service** (+ ses DTOs),
toute l'implémentation réelle (service, repository, entité, mapper) reste cachée.

**Pourquoi le package est-il *plat* (sans sous-dossiers `controllers/`, `services/`…) ?**
C'est une **contrainte technique de Java**, pas un choix esthétique : la visibilité
*package-private* (classe déclarée sans le mot-clé `public`) n'est accessible que
**depuis le même package**. Pour que `AnimeServiceImpl` (caché) puisse utiliser
`AnimeRepository` (caché), les deux **doivent** être dans le même package.

Si l'on conservait des sous-packages par couche, on serait **forcé de tout remettre
en `public`** — et l'on retomberait exactement sur le **Package by Layer**, l'approche
que le cours désigne comme la plus faible en encapsulation. Aplatir le domaine est
donc la condition *sine qua non* pour appliquer réellement les Règles 1 et 2 du sujet.

Ce choix illustre directement le **D de SOLID** (Dependency Inversion) : les
contrôleurs et le composant Watchlist dépendent d'**abstractions** (`AnimeService`,
`WatchlistService`) et jamais des implémentations ou des repositories concrets, qui
demeurent invisibles hors de leur package.

### 4.1 Découplage entre Catalogue et Watchlist
La Watchlist ne dépend **pas** des entités ni du repository du Catalogue. Une entrée
de watchlist référence un anime par son **identifiant** (`animeId`), et la couche
service de la Watchlist vérifie l'existence de l'anime et enrichit ses réponses en
passant par l'**interface** `AnimeService`. L'interaction inter-domaines se fait donc
uniquement via une abstraction, ce qui prépare une éventuelle extraction en
microservices indépendants.

### 4.2 Gestion de l'utilisateur
Le propriétaire d'un suivi est modélisé par une entité `User` **interne au composant
Watchlist** (`fr.miage.numres.watchlist.User`, package-private). Chaque entrée
référence un utilisateur, résolu et validé par le service. L'authentification et la sécurité ne
sont pas dans le périmètre : un utilisateur de démonstration est utilisé par défaut.

### 4.3 API REST (Phase 2)
Les deux domaines exposent l'ensemble des verbes REST sur leurs ressources :
`GET` (collection et élément), `POST`, `PUT` (remplacement complet), `PATCH`
(mise à jour partielle) et `DELETE`. Les contrôleurs ne reçoivent et ne renvoient
que des DTOs (jamais d'entité `@Entity`), et les codes HTTP sont normalisés
(`200`, `201` + `Location`, `204`, `400`, `404`, `409`).

### 4.4 Suppression logique (soft delete) et référence souple résiliente
Comme les deux domaines ne partagent aucune clé étrangère, la suppression d'un anime
pourrait laisser des suivis « orphelins » dans la Watchlist. Plutôt que de subir ce
problème, le Catalogue applique une **suppression logique** :

- l'entité `Anime` porte un drapeau `deleted` (colonne `is_deleted`, `nullable = false`) ;
- `deleteAnime(id)` ne fait **pas** de `delete` SQL : il positionne `deleted = true`
  puis `save` (suppression réversible, traçable) ;
- la **vue publique** ne montre que les animes actifs :
  - `getAllAnimes()` → `repository.findByDeletedFalse()`,
  - `getAnimeById(id)` → `repository.findByIdAndDeletedFalse(id)` (sinon `404`).
  Du point de vue d'un consommateur de l'API, le comportement est **identique à une
  vraie suppression** : l'énoncé « supprimer l'animé du catalogue » est respecté.

La **référence souple** de la Watchlist exploite ce mécanisme via deux contrats distincts
de `AnimeService` :

| Méthode | Visibilité du soft-deleted | Usage |
|---------|----------------------------|-------|
| `getAnimeById(id)` | **exclut** les supprimés (`404`) | validation à l'ajout d'un suivi (on ne suit pas un anime retiré) |
| `findAnimeById(id)` → `Optional` | **inclut** les supprimés | enrichissement en lecture des suivis existants |

Ainsi un suivi dont l'anime a été retiré reste lisible : le `WatchlistEntryDTO` conserve
le `animeTitle` réel et expose `animeAvailable = false` (le client peut afficher
« Attack on Titan — retiré du catalogue »).

**Choix d'implémentation :** pas d'annotation Hibernate `@SQLRestriction`/`@Where`
globale. Une restriction globale filtrerait l'entité dans *toutes* les requêtes,
y compris le `findById` dont la Watchlist a besoin pour résoudre un anime supprimé.
Le filtrage est donc explicite (requêtes dédiées) — on garde le contrôle et on combine
**soft delete** (côté catalogue) et **résilience** (côté watchlist), exactement comme un
système distribué tolère l'indisponibilité d'un service tiers.

## 5. Flux de traitement d'une requête
Exemple : création d'un anime.

1. Le client appelle l'API avec un DTO.
2. Le contrôleur valide la requête.
3. Le service convertit le DTO en entité.
4. Le repository sauvegarde l'entité.
5. Le service reconvertit l'entité en DTO.
6. Le contrôleur renvoie la réponse HTTP.

## 6. Swagger / OpenAPI
Springdoc expose automatiquement la documentation API.
Cela permet :

- de visualiser les endpoints,
- de tester les appels HTTP,
- de partager facilement l'API avec d'autres développeurs.

Endpoints utiles :
- `/swagger-ui.html`
- `/v3/api-docs`

## 7. Base de données
Le projet utilise H2 en mémoire.

Avantages :
- démarrage rapide,
- aucun service externe à installer,
- adapté au développement et au TP.

Limite :
- les données sont perdues au redémarrage.

## 8. Points de vigilance
Lors de la finalisation du projet, il faut vérifier :

- que Spring scanne bien les bons packages,
- que les entités JPA sont bien détectées,
- que les DTOs sont les seuls objets exposés par l'API,
- que les tests valident la couche service,
- que les logs de démarrage ne cachent pas d'erreurs de configuration.

## 9. Évolution future
Déjà réalisé : Catalogue + Watchlist complets, API REST des deux domaines, gestion
d'erreurs normalisée, validation, suppression logique + référence souple résiliente,
suite de tests (unitaires, tranche, persistance, intégration bout-en-bout).

Pistes pour la suite :

- **extraction physique en microservices** : une base par service (`catalog_db`,
  `watchlist_db`) et remplacement de l'appel direct à `AnimeService` par un appel
  réseau (REST/HTTP) ou une synchronisation par événements (ex. publication d'un
  événement « anime retiré » que la Watchlist consomme) ;
- authentification et métier utilisateur réels (Spring Security) ;
- pagination et tri des collections ;
- persistance durable (PostgreSQL) en remplacement de H2.
