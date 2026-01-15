# Repository Guidelines

## Project Structure & Module Organization
- Standard sbt layout: app code in `src/main/scala`, tests in `src/test/scala`, shared configs/assets in `src/main/resources`.
- Core models: `Name`, `Race` (+ `RaceAbility`), `Character` (abilities, AC/HP, gear, talents/features/spells/languages), `CharacterClass` + supporting types (`ClassFeature`, `Talent`, `Spellcasting`, etc.), `Language`, `Spell`, `Item`, `Deity`, `LanguageEntry`.
- Spells: `Spell` model in `src/main/scala/models/Spell.scala` (ObjectId, name, tier, castingAttribute: List[String], optional prohibitedAlignments: List[String], spellType/range/duration/dc, description, damage/healing details, scaling and opposed info). `Spell` has repo/server/route.
- Spells data: `data/spells.json` is valid JSON, alphabetically sorted by `name`, and includes Western Reaches playtest spells.
- Items: `Item` model in `src/main/scala/models/Item.scala` (ObjectId, name, itemType, description, cost, slots, magical flag, AC/defense bonuses, attack traits, versatile/dual-damage, loading flags, etc.). `Item` has repo/server/route and feeds random gear.
- Deities: `Deity` model in `src/main/scala/models/Deity.scala` (ObjectId, name, alignment, description). `Deity` has repo/server/route and seeds random character deities by alignment.
- Languages: `LanguageEntry` model in `src/main/scala/models/LanguageEntry.scala` (ObjectId, name, speakers, rarity). `LanguageEntry` has repo/server/route.
- Repositories/servers/routes: Names and Races (`NameRepository*`, `NameServer`, `NameRoute`; `RaceRepository*`, `RaceServer`, `RaceRoute`); Characters (`CharacterRepository*`, `CharacterServer`, `CharacterRoute`); Character Classes (`CharacterClassRepository*`, `CharacterClassServer`, `CharacterClassRoute`); Personalities (`PersonalityRepository*`, `PersonalityServer`, `PersonalityRoute`); Backgrounds (`BackgroundRepository*`, `BackgroundServer`, `BackgroundRoute`); Spells (`SpellRepository*`, `SpellServer`, `SpellRoute`); Items (`ItemRepository*`, `ItemServer`, `ItemRoute`); Titles (`TitleRepository*`, `TitleServer`, `TitleRoute`); Deities (`DeityRepository*`, `DeityServer`, `DeityRoute`); Languages (`LanguageRepository*`, `LanguageServer`, `LanguageRoute`).
- Config: `application.conf` (server.port, mongodb.uri, optional mongodb.collection defaulting to `Name`); `local-dev.conf` is git-ignored and can override Mongo.
- Data seeding: `data/seed-classes.js` seeds the `Classes` Mongo collection from `data/classes.json`; run via `mongosh --file data/seed-classes.js "$MONGO_URI"` (honors `db=`/`collection=` args). `data/seed-backgrounds.js` seeds `Backgrounds` from `data/backgrounds.json`.
- Deities seeding: `data/seed-deities.js` inserts deities from `data/deities.json`; run via `mongosh --file data/seed-deities.js "$MONGO_URI"` (honors `db=`/`collection=` args or env overrides).
- Languages seeding: `data/seed-languages.js` inserts languages from `data/languages.json`; run via `mongosh --file data/seed-languages.js "$MONGO_URI"` (honors `db=`/`collection=` args or env overrides).
- Spells seeding: `data/seed-spells.js` inserts spells from `data/spells.json`; run via `mongosh --file data/seed-spells.js "$MONGO_URI"` (honors `db=`/`collection=` args or env overrides).
  - Western Reaches source PDF is stored at `reference_data/Western Reaches Spells (Playtest).pdf` for reference when updating spell data.
- Titles seeding: `data/seed-titles.js` inserts titles from `data/titles.json`; run via `mongosh --file data/seed-titles.js "$MONGO_URI"` (honors `db=`/`collection=` args or env overrides).
- Items data/seeding: `data/items.json`/`data/items-merged.json` hold merged SD2FG + BFRPG gear/weapon/armor sets (with slots, versatility, loading, defense bonuses, magic flag, zero-slot gear, deduped names). Seed via `data/seed-items.js` (`mongosh --file data/seed-items.js "$MONGO_URI"`).
- Zero-level gear list: `src/main/resources/zero-level-gear.json` provides the 0-level gear table for the random character generator.
- Settlement map assets: `src/main/resources/settlement_assets/README.txt` lists required/optional PNGs for `/settlements/random.png`.

## Build, Test, and Development Commands
- `sbt compile` — compile and fetch deps (Mongo driver 5.6.1, ZIO 2.1.23, zio-http 3.6.0, zio-json 0.7.0, PDFBox 2.0.30).
- `sbt run` — launch the ZIO HTTP app; binds to `server.port` (override with `PORT`/`SERVER_PORT`).
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
- Mongo: set `mongodb.uri`/`mongo.uri`; `mongodb.collection` defaults to `Name`. Override DB via URI or config.
- Validate input at HTTP boundaries; avoid unbounded Mongo queries or user-controlled filters without limits.
- Scrub sensitive fields before logging; keep logs minimal in production.

## HTTP & Persistence Notes
- HTTP: zio-http server in `Main.scala` exposes `/health`, `/`, `/names` (list names), `/races` (list races), `/random-character` (random or stored character; supports `?zeroLevel=true|1` for 0-level), `/random-character.pdf` (fills `src/main/resources/ShadowdarkSheet.pdf` AcroForm with a random character; supports `?zeroLevel=true|1`), `/personalities` (list/create), `/backgrounds` (list), `/classes` (list), `/spells` (list), `/items` (list), `/titles` (list), `/deities` (list), `/languages` (list).
- Models: `Name`, `Race` (+ `RaceAbility`), `Character` (abilities, HP/AC, gear, features, talents, spells, languages), `CharacterClass` (+ `ClassFeature`, `Talent`, `Spellcasting`, etc.), `Language`, `LanguageEntry`, `Spell`, `Item`, `Title`, `Deity`; all have zio-json codecs and Mongo ObjectId encode/decode where needed.
- Persistence: `NameRepository`, `RaceRepository`, `CharacterRepository`, `CharacterClassRepository`, `SpellRepository`, `ItemRepository` with live Mongo implementations using codec registries. Race codec registry must include `RaceAbility`; class registry must include class support types.
- Characters: `CharacterServer` builds random characters with weighted races, class-driven HP, features, talents (rolled 2d6 per talent level), spell summaries, gear, and language selection (Common + race languages + class-granted picks, resolving “extra” placeholders to actual common/rare languages). Gear slots are filled from Items (class weapon/armor allowances, slots, deduped gear, ammo for ranged, crawling-kit fillers; zero-slot gear goes to `freeToCarry`). `CharacterRoute` renders the random character both as JSON and as a filled PDF using the bundled sheet template.
- Caching: `NameServer` and `RaceServer` cache list fetches for 5 minutes via zio-cache (capacity 1) and invalidate on create.
- Names API: `/names` GET returns cached list; POST accepts JSON (`name`, `race`, optional `gender`, `firstName`, `lastName`) and inserts a new ObjectId-backed `Name`, returns 201 JSON and clears cache.

## Settlement Generator Notes (WIP)
- Current status: settlement maps are generated via `SettlementServer` with Voronoi districts, organic outer boundary, lightweight building footprints, plazas, POI markers, and banner labels.
- PNG endpoint: `/settlements/random.png` renders the map with clipped boundary and legend index entries; use a local save to inspect (e.g., `random.png` in repo root).
- New data/model additions:
  - `NpcQuality` model and repo/server for NPC qualities (`appearance`, `does`, `secret`, plus weighted `age` and `wealth` entries).
  - Seed files: `data/npc-qualities.json`, `data/seed-npc-qualities.js`.
  - Settlement model now includes `Npc`, `Building`, `Plaza`, and `PointOfInterest` fields (IDs and locations), plus `District.boundary` and `District.plazas`.
- NPC generation uses: Name/Race/Personality/Background servers + NpcQuality entries; backgrounds are matched by keyword vs POI type.
- Visual tweaks implemented:
  - Non-rectangular outer boundary built from district union + smoothing; map content is clipped to it.
  - Buildings are clustered but partially aligned to a main road axis; plazas keep a clear center area.
  - Main roads have thicker stroke; minor roads thinner and fewer.
  - POI markers snap near POI-assigned buildings.
  - Grid opacity reduced.
  - District labels are banner-style boxes, but they still sometimes truncate at edges.

## Next Steps (Resume Tomorrow)
- Fix district label truncation:
  - Labels are clamped to map bounds but can still overflow near the organic boundary.
  - Consider: clamping to boundary polygon bounds, shrinking font if banner would exceed boundary, or offsetting labels toward district centroid.
- Re-check legend formatting/spacing if banner size changes.
- Confirm POI marker snap behavior vs buildings after label adjustments.

## Settlement Roadmap (Phases)
### Phase 1 (Current)
- Organic boundary, Voronoi wards, curved roads with hierarchy, clustered buildings with partial road alignment, plazas, POI markers, softer grid.
- Remaining issue: district label truncation near boundary.
### Phase 1 Summary
- Replaced on-map district labels with a right-side District Key; keys moved to a second PDF page rendered by `/settlements/random.pdf`.
- District types are unique when the settlement has fewer than eight districts.
- Legend formatting improved with bold district headers and spacing; keyed index runs on its own page.
- POI markers now stay anchored to their assigned building footprints.

### Phase 2 (Layout Improvements)
- Add a main street loop or radial spine from the seat with clearer connectivity.
- Make plazas influence roads/buildings (larger open spaces, road-fronting buildings).
- Increase building density along roads and near hubs; reduce near boundaries.
### Phase 2 Progress
- Implemented a main street loop connecting districts around the seat.
- Added road-aware building alignment with frontage bias plus a 14px road keep-out buffer.

### Phase 3 (Polish)
- Improve legend formatting/spacing with small icons for POI types.
- Add subtle ward-themed textures or color variation (lightweight).
- Optionally vary building shapes by ward (temples larger, markets stall-like).
