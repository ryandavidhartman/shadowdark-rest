# Repository Guidelines

## Project Structure & Module Organization
- Standard sbt layout: app code in `src/main/scala`, tests in `src/test/scala`, shared configs/assets in `src/main/resources`.
- Current features: `models/Name.scala`, `repositories/*`, `servers/NameServer.scala`, `names/NameRoute.scala`.
- Config: `application.conf` (server.port, mongodb.uri, optional mongodb.collection defaulting to `Name`); `local-dev.conf` is git-ignored and can override Mongo.

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
- HTTP: zio-http server in `Main.scala` exposes `/health`, `/`, and `/names` (list names).
- Model: `models.Name` with zio-json codecs and Mongo `ObjectId` encode/decode.
- Persistence: `NameRepository` trait with `NameRepositoryLive` using Mongo codec registries.
