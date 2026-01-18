# Repository Guidelines

## Project Structure & Module Organization
- Standard sbt layout: app code in `src/main/scala`, tests in `src/test/scala`, shared configs/assets in `src/main/resources`.
- Core models: `Name`, `Race` (+ `RaceAbility`), `Character` (abilities, AC/HP, gear, talents/features/spells/languages), `CharacterClass` + supporting types (`ClassFeature`, `Talent`, `Spellcasting`, etc.), `Background`, `Personality`, `Title`, `Monster`, `Deity`, `Language`, `LanguageEntry`, `Spell`, `Item`, `NpcQuality`, `SettlementName`, `Settlement`.
- Spells: `Spell` model in `src/main/scala/models/Spell.scala` (ObjectId, name, tier, castingAttribute: List[String], optional prohibitedAlignments: List[String], spellType/range/duration/dc, description, damage/healing details, scaling and opposed info). `Spell` has repo/server/route.
- Spells data: `data/spells.json` is valid JSON, alphabetically sorted by `name`; use `reference_data/Western Reaches Spells (Playtest).pdf` when updating playtest spells.
- Items: `Item` model in `src/main/scala/models/Item.scala` (ObjectId, name, itemType, description, cost, slots, magical flag, AC/defense bonuses, attack traits, versatile/dual-damage, loading flags, etc.). `Item` has repo/server/route and feeds random gear.
- Deities: `Deity` model in `src/main/scala/models/Deity.scala` (ObjectId, name, alignment, description). `Deity` has repo/server/route and seeds random character deities by alignment.
- Languages: `LanguageEntry` model in `src/main/scala/models/LanguageEntry.scala` (ObjectId, name, speakers, rarity). `LanguageEntry` has repo/server/route.
- Repositories/servers/routes: Names and Races (`NameRepository*`, `NameServer`, `NameRoute`; `RaceRepository*`, `RaceServer`, `RaceRoute`); Characters (`CharacterRepository*`, `CharacterServer`, `CharacterRoute`); Character Classes (`CharacterClassRepository*`, `CharacterClassServer`, `CharacterClassRoute`); Personalities (`PersonalityRepository*`, `PersonalityServer`, `PersonalityRoute`); Backgrounds (`BackgroundRepository*`, `BackgroundServer`, `BackgroundRoute`); Spells (`SpellRepository*`, `SpellServer`, `SpellRoute`); Items (`ItemRepository*`, `ItemServer`, `ItemRoute`); Titles (`TitleRepository*`, `TitleServer`, `TitleRoute`); Deities (`DeityRepository*`, `DeityServer`, `DeityRoute`); Languages (`LanguageRepository*`, `LanguageServer`, `LanguageRoute`); Monsters (`MonsterRepository*`, `MonsterServer`, `MonsterRoute`); Settlement names (`SettlementNameRepository*`, `SettlementNameServer`, `SettlementNameRoute`); Settlements (`SettlementServer`, `SettlementRoute`); NPC qualities (`NpcQualityRepository*`, `NpcQualityServer`, no route).
- Config: `application.conf` (server.port, mongodb.uri); repositories honor optional `mongo.uri`/`mongodb.uri`, `mongo.database`/`mongodb.database`, and `mongo.collection`/`mongodb.collection` with repo-specific defaults (for example, `Names`).
- Data seeding: `data/seed-classes.js` seeds the `Classes` Mongo collection from `data/classes.json`; run via `mongosh --file data/seed-classes.js "$MONGO_URI"` (honors `db=`/`collection=` args). `data/seed-backgrounds.js` seeds `Backgrounds` from `data/backgrounds.json`.
- Deities seeding: `data/seed-deities.js` inserts deities from `data/deities.json`; run via `mongosh --file data/seed-deities.js "$MONGO_URI"` (honors `db=`/`collection=` args or env overrides).
- Languages seeding: `data/seed-languages.js` inserts languages from `data/languages.json`; run via `mongosh --file data/seed-languages.js "$MONGO_URI"` (honors `db=`/`collection=` args or env overrides).
- Spells seeding: `data/seed-spells.js` inserts spells from `data/spells.json`; run via `mongosh --file data/seed-spells.js "$MONGO_URI"` (honors `db=`/`collection=` args or env overrides).
  - Western Reaches source PDF is stored at `reference_data/Western Reaches Spells (Playtest).pdf` for reference when updating spell data.
- Titles seeding: `data/seed-titles.js` inserts titles from `data/titles.json`; run via `mongosh --file data/seed-titles.js "$MONGO_URI"` (honors `db=`/`collection=` args or env overrides).
- Monsters seeding: `data/seed-monsters.js` inserts monsters from `data/monsters.json`; run via `mongosh --file data/seed-monsters.js "$MONGO_URI"` (honors `db=`/`collection=` args or env overrides).
- Settlement names seeding: `data/seed-settlement-names.js` inserts settlement names from `data/settlement-names.json`; run via `mongosh --file data/seed-settlement-names.js "$MONGO_URI"` (honors `db=`/`collection=` args or env overrides).
- Items data/seeding: `data/items.json` contains item data used for seeding and gear generation (slots, loading, bonuses, magic flag, deduped names). Seed via `data/seed-items.js` (`mongosh --file data/seed-items.js "$MONGO_URI"`).
- Zero-level gear list: `src/main/resources/zero-level-gear.json` provides the 0-level gear table for the random character generator.

## Build, Test, and Development Commands
- `sbt compile` — compile and fetch deps (Mongo driver 5.6.2, ZIO 2.1.24, zio-http 3.7.4, zio-json 0.7.44, zio-cache 0.2.7, PDFBox 3.0.6, JTS 1.18.1).
- `sbt run` — launch the ZIO HTTP app; binds to `server.port` (override with `SERVER_PORT`).
- `sbt test` — run the suite; add your test framework dependency in `build.sbt` if absent.
- `sbt console` — REPL with project classes for quick checks; `sbt clean` to clear compiled artifacts.

## Coding Style & Naming Conventions
- Target Scala 2.13; prefer ZIO over `Future`.
- Two-space indentation, ~120-char lines, expression-oriented code, minimal vars/mutability.
- Name packages by feature (`names`, `repositories`, `servers`); files after the main type; keep side effects at boundaries.

## Testing Guidelines
- Place tests in `src/test/scala`, mirroring package paths.
- Use a single framework (ZIO Test); declare it in `build.sbt` with any fixtures (embedded Mongo, fakes).
- Behavior-focused names: `"CharacterRepository" should "persist and fetch by id"`. Cover success, failure, and edge cases (empty collections, invalid payloads).
- Keep unit tests fast and deterministic; mark slower Mongo integrations with tags so they can be skipped when needed.

## Commit & Pull Request Guidelines
- Commits: imperative, concise subjects (`Add character routes`, `Fix Mongo codec registration`); keep diffs focused.
- Pull requests: brief intent, key changes, schema/API contract notes, linked issues, and test evidence (`sbt test`). Add screenshots for any API docs or UI artifacts.

## Security & Configuration Tips
- Never commit secrets; load Mongo URIs and credentials from environment variables or git-ignored configs (`local-dev.conf`).
- Mongo: set `mongodb.uri`/`mongo.uri`; collections default per repo (for example, `Names`). Override DB via URI or `mongo.database`/`mongodb.database`.
- Validate input at HTTP boundaries; avoid unbounded Mongo queries or user-controlled filters without limits.
- Scrub sensitive fields before logging; keep logs minimal in production.

## HTTP & Persistence Notes
- HTTP: zio-http server in `Main.scala` exposes `/health`, `/`, `/names` (list/create), `/races` (list), `/random-character` (random or stored character; supports `?zeroLevel=true|1` for 0-level), `/random-character.pdf` (fills `src/main/resources/ShadowdarkSheet.pdf` AcroForm with a random character; supports `?zeroLevel=true|1`), `/personalities` (list/create), `/backgrounds` (list), `/classes` (list), `/spells` (list), `/items` (list), `/titles` (list), `/deities` (list), `/languages` (list), `/monsters` (list), `/settlement-names` (list), `/settlements/random` (random settlement JSON), `/settlements/random.pdf` (settlement map PDF with keyed index).
- Models: `Name`, `Race` (+ `RaceAbility`), `Character` (abilities, HP/AC, gear, features, talents, spells, languages), `CharacterClass` (+ `ClassFeature`, `Talent`, `Spellcasting`, etc.), `Background`, `Personality`, `Title`, `Monster`, `Deity`, `Language`, `LanguageEntry`, `Spell`, `Item`, `NpcQuality`, `SettlementName`, `Settlement`; all have zio-json codecs and Mongo ObjectId encode/decode where needed.
- Persistence: `NameRepository`, `RaceRepository`, `CharacterRepository`, `CharacterClassRepository`, `PersonalityRepository`, `BackgroundRepository`, `SpellRepository`, `ItemRepository`, `TitleRepository`, `DeityRepository`, `LanguageRepository`, `MonsterRepository`, `NpcQualityRepository`, `SettlementNameRepository` with live Mongo implementations using codec registries. Race codec registry must include `RaceAbility`; class registry must include class support types.
- Characters: `CharacterServer` builds random characters with weighted races, class-driven HP, features, talents (rolled 2d6 per talent level), spell summaries, gear, and language selection (Common + race languages + class-granted picks, resolving “extra” placeholders to actual common/rare languages). Gear slots are filled from Items (class weapon/armor allowances, slots, deduped gear, ammo for ranged, crawling-kit fillers; zero-slot gear goes to `freeToCarry`). `CharacterRoute` renders the random character both as JSON and as a filled PDF using the bundled sheet template.
- Caching: `NameServer` caches list fetches for 5 minutes via zio-cache (capacity 1) and invalidates on create; `RaceServer`, `MonsterServer`, `NpcQualityServer`, and `SettlementNameServer` cache list fetches for 5 minutes.
- Names API: `/names` GET returns cached list; POST accepts JSON (`name`, `race`, optional `gender`, `firstName`, `lastName`) and inserts a new ObjectId-backed `Name`, returns 201 JSON and clears cache.

## Settlement Generator Notes (WIP)
- Current status: `SettlementServer` generates settlement JSON and a PDF map with Voronoi districts, an organic outer boundary, building footprints, plazas, POI markers, and a keyed district index.
- Endpoints: `/settlements/random` returns settlement JSON; `/settlements/random.pdf` renders the map on page 1 (POI numbers only) and the keyed index on page 2 (district labels and entries).
- PNG rendering: `renderSettlementPng` exists but no HTTP route currently exposes it.
- Voronoi flow (code path):
  1) Seed points are generated within a city mask.
  2) Voronoi cells are computed from those points.
  3) Each cell becomes a district boundary; districts are assigned types/rolls.
  4) The settlement outline is built by unioning/smoothing district polygons; map content is clipped to this outline.
  5) Roads are built from district positions (seat + ring loop + nearest neighbors), then used as anchors for building alignment.
  6) Plazas/POIs/buildings are placed inside district boundaries; PDF renders page 1 map + page 2 keyed index.
- Data/model additions:
  - `NpcQuality` model and repo/server for NPC qualities (`appearance`, `does`, `secret`, weighted `age`, weighted `wealth`).
  - Seed files: `data/npc-qualities.json`, `data/seed-npc-qualities.js`.
  - Settlement model includes `Npc`, `Building`, `Plaza`, `PointOfInterest` (IDs and locations), plus `District.boundary` and `District.plazas`.
- NPC generation: uses Name/Race/Personality/Background servers + NpcQuality entries; backgrounds are matched by keyword to POI type.
- Settlement naming: uses `SettlementNameServer` entries seeded from `data/settlement-names.json`.
- Visual behavior:
  - Boundary is a smoothed union of districts; map content is clipped to it.
  - Buildings are clustered with partial alignment to a main road axis; plazas preserve a clear center.
  - Main roads are thicker; minor roads are thinner and fewer.
  - POI markers snap near their assigned buildings.
  - Grid opacity is reduced.
## Settlement Quick Verification Checklist
- Routes: `/settlements/random` JSON and `/settlements/random.pdf` PDF both respond.
- PDF: page 1 map shows POI numbers only; page 2 shows the keyed index with district headers and POI/NPC lines.
- Roads: ring loop between non-seat districts exists when 3+ districts; main connectors from seat are thicker.
- Buildings: align to main roads with a 14px road keep-out buffer; plazas keep open centers.
- NPCs: POIs include NPCs built from Name/Race/Personality/Background/NpcQuality sources.
## Settlement Known Risks / Caveats
- Legend overflow: long POI/NPC text or many districts can exceed page 2 space and truncate without warning.
- Building underfill: `generateBuildingFootprints` uses a max-attempts loop; dense districts can end with fewer buildings than target.
- Road loop condition: the ring loop only exists when 3+ non-seat districts are present; smaller settlements will lack a loop.
- PNG output: `renderSettlementPng` exists but no route exposes it, so PDF is the only supported output.
## Settlement Code Pointers
- Entry point: `src/main/scala/servers/SettlementServer.scala` (`randomSettlement`, `renderSettlementPdf`, `drawLegend`, road/building generation).
- Routes: `src/main/scala/routes/SettlementRoute.scala` (JSON + PDF endpoints).
- Models: `src/main/scala/models/Settlement.scala`, `src/main/scala/models/NpcQuality.scala`, `src/main/scala/models/SettlementName.scala`.
## Settlement Function Map
- Seeding points + Voronoi: `buildCityMask`, `buildVoronoiCells`.
- Outline: `buildSettlementOutline` (uses district boundaries, smoothing/roughening).
- Roads: `buildRoadEdgesForPoints` (seed + ring), `buildRoadEdges` (district graph), `drawRoads`.
- Buildings/plazas: `generateBuildingFootprints`, `drawPlazas`, `drawBuildings`.
- Output: `randomSettlement`, `renderSettlementPdf`, `drawLegend`.

## Settlement Random Settlement Generation
- Organic boundary, Voronoi wards, curved roads with hierarchy, clustered buildings with partial road alignment, plazas, POI markers, softer grid.
- Replaced on-map district labels with a right-side District Key; keys moved to a second PDF page rendered by `/settlements/random.pdf`.
- District types are unique when the settlement has fewer than eight districts.
- Legend formatting improved with bold district headers and spacing; keyed index runs on its own page.
- POI markers now stay anchored to their assigned building footprints.

