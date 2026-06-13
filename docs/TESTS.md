# Stratégie de tests — AnimeTracker

Ce document interprète l'ensemble de la suite de tests du projet : **53 tests, 0 échec**,
répartis en 5 niveaux. Pour *lancer* les tests, voir [`PRESENTATION.md` §4](PRESENTATION.md).

## Vue d'ensemble — la pyramide des tests

| Niveau | Classes | Tests | Mocké ? | But |
|--------|---------|-------|---------|-----|
| Unitaire (service) | 2 | 23 | tout sauf le service | valider la **logique métier** |
| Slice web (`@WebMvcTest`) | 2 | 18 | le service | valider la **couche HTTP** |
| Persistance (`@DataJpaTest`) | 1 | 1 | rien (H2 réel) | valider le **mapping JPA** |
| Intégration E2E (`@SpringBootTest`) | 2 | 10 | rien (pile réelle) | valider **toutes les couches ensemble** |
| Contexte Spring | 1 | 1 | — | valider le **démarrage** |
| **Total** | **8** | **53** | | |

C'est une pyramide saine : beaucoup de tests unitaires rapides à la base, peu de tests
d'intégration lourds au sommet.

---

## 1. Tests unitaires — logique métier isolée (23 tests)

**Principe :** on teste **une seule classe** (le service), coupée de la base et du serveur.
Ses dépendances sont remplacées par des **mocks** (Mockito) dont on contrôle les réponses.
Très rapides (millisecondes), ils prouvent que les règles métier sont correctes.

> Annotations clés : `@ExtendWith(MockitoExtension.class)`, `@Mock`, `@InjectMocks`.

### `catalog/AnimeServiceImplTest` (11 tests) — catalogue + soft delete

| Test | Ce qu'il prouve |
|------|-----------------|
| `getAllAnimes_returnsOnlyActiveMappedDtos` | la liste passe par `findByDeletedFalse()` → les supprimés sont masqués |
| `getAnimeById_whenActive` / `whenMissingOrDeleted_throwsNotFound` | anime actif renvoyé ; absent **ou** supprimé → `404` |
| `findAnimeById_whenMissing_returnsEmpty` / `includesSoftDeleted` | la recherche d'enrichissement retrouve **même** un anime supprimé |
| `createAnime_savesEntityAndReturnsDto` | création + mapping DTO↔entité |
| `replaceAnime` / `patchAnime` (whenActive / whenMissing) | PUT et PATCH n'agissent que sur un anime actif |
| `deleteAnime_whenActive_marksAsDeletedInsteadOfRemoving` | **cœur du soft delete** : un `ArgumentCaptor` vérifie `deleted=true` sauvegardé et `deleteById` jamais appelé |
| `deleteAnime_whenMissing_throwsNotFound` | suppression d'un inexistant → `404`, aucune sauvegarde |

### `watchlist/WatchlistServiceImplTest` (12 tests) — règles de suivi + résilience

| Test | Ce qu'il prouve |
|------|-----------------|
| `addEntry_whenValid_enrichesWithAnimeAndUser` | suivi enrichi (titre, épisodes, username) ; `animeAvailable=true` |
| `addEntry_whenStatusCompleted_setsProgressToTotal` | règle : `COMPLETED` aligne la progression sur le total |
| `addEntry_whenProgressExceedsTotal_throwsBusinessRule` | règle : progression > épisodes → `400` |
| `addEntry_whenAnimeMissing_propagatesNotFound` | suivre un anime inexistant → `404` |
| `addEntry_whenUserMissing_throwsUserNotFound` | utilisateur inconnu → `404` |
| `addEntry_whenDuplicate_throwsConflict` | doublon (user, anime) → `409` |
| `replaceEntry` / `patchEntry` | PUT/PATCH appliquent le mapping et renvoient le DTO enrichi |
| `getEntryById_whenReferencedAnimeDeleted_returnsDtoWithoutAnimeInfo` | **résilience** : anime introuvable → titre `null`, `animeAvailable=false`, pas de crash |
| `getEntryById_whenAnimeSoftDeleted_keepsTitleButMarksUnavailable` | **soft delete** : titre conservé, `animeAvailable=false` |
| `getEntryById_whenMissing` / `deleteEntry_whenMissing` | suivi inexistant → `404` |

---

## 2. Tests d'intégration — toute la pile réelle (10 tests)

**Principe :** à l'opposé des tests unitaires, **rien n'est mocké**. Le contexte Spring
complet démarre et on traverse `Controller → Service → Repository → base H2` via de vraies
requêtes HTTP (`MockMvc`). Profil `test` (base vide, déterministe), `@Transactional`
(rollback après chaque test → isolation).

> Annotations clés : `@SpringBootTest`, `@AutoConfigureMockMvc`, `@ActiveProfiles("test")`, `@Transactional`.

### `catalog/CatalogIntegrationTest` (4 tests)

| Test | Ce qu'il prouve |
|------|-----------------|
| `createThenGetById_persistsThroughWholeStack` | un anime créé via `POST` est réellement persisté et relu |
| `getById_whenMissing_returns404WithApiError` | `404` avec le format `ApiError` |
| `create_whenInvalidPayload_returns400WithFieldErrors` | validation Jakarta réelle → `400` + `fieldErrors` |
| `delete_isLogical_animeDisappearsFromApiButGetReturns404` | **soft delete E2E** : après `DELETE`, `404` + absent de la liste |

### `watchlist/WatchlistIntegrationTest` (6 tests)

| Test | Ce qu'il prouve |
|------|-----------------|
| `addThenPatchProgression_updatesThroughWholeStack` | ajout puis progression `PATCH` persistés |
| `addEntry_whenAnimeDoesNotExist_returns404` | référence vers un anime inexistant rejetée |
| `addEntry_whenDuplicate_returns409` | contrainte d'unicité (user, anime) réelle en base |
| `addEntry_whenAnimeIdMissing_returns400` | validation à l'entrée |
| `addEntry_whenProgressExceedsEpisodes_returns400BusinessRule` | règle métier sur la vraie pile |
| `softDeleteAnime_thenWatchlistEntryStaysReadableButUnavailable` | **scénario phare inter-domaines** : anime suivi supprimé → suivi lisible, `animeAvailable=false` |

> 💡 `WatchlistIntegrationTest` prépare son utilisateur via le `UserRepository`
> *package-private* : c'est possible car le test est dans le **même package** que le
> composant. L'encapsulation choisie n'empêche donc pas de tester finement.

---

## 3. Les autres tests (20 tests)

### a) Tests de tranche web (« slice », `@WebMvcTest`) — 18 tests
Testent **uniquement la couche web** (routing, JSON, codes HTTP, validation) : seul le
contrôleur démarre, le service est **mocké** (`@MockitoBean`). Plus légers et plus ciblés
qu'un test d'intégration.
- `catalog/AnimeControllerTest` (9) : tous les verbes `/api/catalog`, codes `200/201+Location/204/400/404`, JSON.
- `watchlist/WatchlistControllerTest` (9) : tous les verbes `/api/watchlist`, filtres `?userId=`/`?status=`, présence de `animeAvailable`.

### b) Test de persistance (`@DataJpaTest`) — 1 test
- `catalog/AnimeRepositoryTest` : démarre **seulement** la couche JPA + H2 et vérifie qu'une
  entité `Anime` est sauvegardée puis relue (valide le mapping objet↔table).

### c) Test de chargement du contexte — 1 test
- `SpringDemoNumres2526ApplicationTests` : vérifie que **toute l'application démarre sans
  erreur de configuration** (tous les beans s'assemblent). Filet de sécurité de base.

---

## 4. Différence clé : slice vs intégration

| | `@WebMvcTest` (slice) | `@SpringBootTest` (intégration) |
|--|----------------------|--------------------------------|
| Démarre | seulement le contrôleur ciblé | **toute** l'application |
| Service | **mocké** (`@MockitoBean`) | **réel** |
| Base de données | absente | **H2 réelle** |
| Vitesse | rapide | plus lent |
| Question répondue | « le contrôleur expose-t-il bien le bon JSON / code ? » | « les couches fonctionnent-elles **vraiment ensemble** ? » |
