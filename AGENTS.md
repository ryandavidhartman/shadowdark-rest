# Repository Guidelines

## Project Structure & Module Organization
- Standard sbt layout: app code in `src/main/scala`, tests in `src/test/scala`, shared configs/assets in `src/main/resources`.
- Core models: `Name`, `Race` (+ `RaceAbility`), `Character` (abilities, AC/HP, gear, talents/features/spells/languages), `CharacterClass` + supporting types (`ClassFeature`, `Talent`, `Spellcasting`, etc.), `Language`.
- Spells: `Spell` model in `src/main/scala/models/Spell.scala` (ObjectId, name, tier, castingAttribute: List[String], optional prohibitedAlignments: List[String], spellType/range/duration/dc, description, damage/healing details, scaling and opposed info).
- Repositories/servers/routes: Names and Races (`NameRepository*`, `NameServer`, `NameRoute`; `RaceRepository*`, `RaceServer`, `RaceRoute`); Characters (`CharacterRepository*`, `CharacterServer`, `CharacterRoute`); Character Classes (`CharacterClassRepository*`, `CharacterClassServer`, `CharacterClassRoute`); Personalities (`PersonalityRepository*`, `PersonalityServer`, `PersonalityRoute`); Backgrounds (`BackgroundRepository*`, `BackgroundServer`, `BackgroundRoute`).
- Config: `application.conf` (server.port, mongodb.uri, optional mongodb.collection defaulting to `Name`); `local-dev.conf` is git-ignored and can override Mongo.
- Data seeding: `data/seed-classes.js` seeds the `Classes` Mongo collection with core class data; run via `mongosh --file data/seed-classes.js "$MONGO_URI"`. `data/seed-backgrounds.js` seeds `Backgrounds` (CLI URI/db/collection args or env supported).
- Spells seeding: `data/seed-spells.js` inserts 268 spells parsed from the SD2FG XML export; run via `mongosh --file data/seed-spells.js "$MONGO_URI"` (honors `db=`/`collection=` args or env overrides).

## Build, Test, and Development Commands
- `sbt compile` — compile and fetch deps (Mongo driver 5.6.1, ZIO 2.1.23, zio-http 3.6.0, zio-json 0.7.0).
- `sbt run` — launch the ZIO HTTP app; binds to `server.port` (override with `PORT`/`SERVER_PORT`).
- `sbt test` — run the suite; add your test framework dependency in `build.sbt` if absent.
- `sbt console` — REPL with project classes for quick checks; `sbt clean` to clear compiled artifacts.

## Coding Style & Naming Conventions
- Target Scala 2.13; prefer ZIO over `Future`.
- Two-space indentation, ~120-char lines, expression-oriented code, minimal vars/mutability.
- Name packages by feature (`names`, `repositories`, `servers`); files after the main type; keep side effects at boundaries.

## Testing Guidelines
- Place tests in `src/test/scala`, mirroring package paths.
- Use a single framework (ScalaTest or MUnit); declare it in `build.sbt` with any fixtures (embedded Mongo, fakes).
- Behavior-focused names: `"CharacterRepository" should "persist and fetch by id"`. Cover success, failure, and edge cases (empty collections, invalid payloads).
- Keep unit tests fast and deterministic; mark slower Mongo integrations with tags so they can be skipped when needed.

## Commit & Pull Request Guidelines
- Commits: imperative, concise subjects (`Add character routes`, `Fix Mongo codec registration`); keep diffs focused.
- Pull requests: brief intent, key changes, schema/API contract notes, linked issues, and test evidence (`sbt test`). Add screenshots for any API docs or UI artifacts.

## Security & Configuration Tips
- Never commit secrets; load Mongo URIs and credentials from environment variables or git-ignored configs (`local-dev.conf`).
- Mongo: set `mongodb.uri`/`mongo.uri`; `mongodb.collection` defaults to `Name`. Override DB via URI or config.
- Validate input at HTTP boundaries; avoid unbounded Mongo queries or user-controlled filters without limits.
- Scrub sensitive fields before logging; keep logs minimal in production.

## HTTP & Persistence Notes
- HTTP: zio-http server in `Main.scala` exposes `/health`, `/`, `/names` (list names), `/races` (list races), `/random-character` (random or stored character), `/personalities` (list/create), `/backgrounds` (list), `/classes` (list).
- Models: `Name`, `Race` (+ `RaceAbility`), `Character` (abilities, HP/AC, gear, features, talents, spells, languages), `CharacterClass` (+ `ClassFeature`, `Talent`, `Spellcasting`, etc.), `Language`; all have zio-json codecs and Mongo ObjectId encode/decode where needed.
- Persistence: `NameRepository`, `RaceRepository`, `CharacterRepository`, `CharacterClassRepository` with live Mongo implementations using codec registries. Race codec registry must include `RaceAbility`; class registry must include class support types.
- Characters: `CharacterServer` builds random characters with weighted races, class-driven HP, features, talents (rolled 2d6 per talent level), spell summaries, gear, and language selection (Common + race languages + class-granted picks, resolving “extra” placeholders to actual common/rare languages).
- Caching: `NameServer` and `RaceServer` cache list fetches for 5 minutes via zio-cache (capacity 1) and invalidate on create.
- Names API: `/names` GET returns cached list; POST accepts JSON (`name`, `race`, optional `gender`, `firstName`, `lastName`) and inserts a new ObjectId-backed `Name`, returns 201 JSON and clears cache.
