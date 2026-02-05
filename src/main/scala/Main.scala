import com.typesafe.config.ConfigFactory
import routes.{
  BackgroundRoute,
  CharacterClassRoute,
  CharacterRoute,
  DeityRoute,
  DungeonRoute,
  HexMapRoute,
  ItemRoute,
  LanguageRoute,
  MonsterRoute,
  NameRoute,
  PersonalityRoute,
  RaceRoute,
  SettlementNameRoute,
  SettlementRoute,
  SpellRoute,
  SwaggerRoutes,
  TitleRoute,
}
import repositories.{
  BackgroundRepositoryLive,
  CharacterClassRepositoryLive,
  CharacterRepositoryLive,
  DeityRepositoryLive,
  ItemRepositoryLive,
  LanguageRepositoryLive,
  MonsterRepositoryLive,
  NameRepositoryLive,
  NpcQualityRepositoryLive,
  PersonalityRepositoryLive,
  RaceRepositoryLive,
  SettlementNameRepositoryLive,
  SpellRepositoryLive,
  TitleRepositoryLive,
}
import servers.{
  BackgroundServer,
  CharacterClassServer,
  CharacterServer,
  DeityServer,
  DungeonServer,
  HexMapServer,
  ItemServer,
  LanguageServer,
  MonsterServer,
  NameServer,
  NpcQualityServer,
  PersonalityServer,
  RaceServer,
  SettlementNameServer,
  SettlementServer,
  SpellServer,
  TitleServer,
}
import zio._
import zio.http._
import zio.http.Method

object Main extends ZIOAppDefault {

  // Minimal health endpoint to verify the service is running.
  private val app = Routes(
    Method.GET / "health" -> handler(Response.text("ok")),
    Method.GET / Root -> handler(Response.text("Shadowdark REST is live"))
  )

  override val run =
    ZIO
      .attempt(ConfigFactory.load().getInt("server.port"))
      .flatMap { port =>
        val program = for {
          nameRoutes      <- ZIO.serviceWith[NameRoute](_.routes)
          raceRoutes      <- ZIO.serviceWith[RaceRoute](_.routes)
          characterRoutes <- ZIO.serviceWith[CharacterRoute](_.routes)
          personalityRoutes <- ZIO.serviceWith[PersonalityRoute](_.routes)
          backgroundRoutes  <- ZIO.serviceWith[BackgroundRoute](_.routes)
          classRoutes       <- ZIO.serviceWith[CharacterClassRoute](_.routes)
          spellRoutes       <- ZIO.serviceWith[SpellRoute](_.routes)
          itemRoutes        <- ZIO.serviceWith[ItemRoute](_.routes)
          titleRoutes       <- ZIO.serviceWith[TitleRoute](_.routes)
          deityRoutes       <- ZIO.serviceWith[DeityRoute](_.routes)
          languageRoutes    <- ZIO.serviceWith[LanguageRoute](_.routes)
          monsterRoutes     <- ZIO.serviceWith[MonsterRoute](_.routes)
          settlementNameRoutes <- ZIO.serviceWith[SettlementNameRoute](_.routes)
          settlementRoutes  <- ZIO.serviceWith[SettlementRoute](_.routes)
          dungeonRoutes     <- ZIO.serviceWith[DungeonRoute](_.routes)
          hexMapRoutes      <- ZIO.serviceWith[HexMapRoute](_.routes)
          swaggerRoutes     <- ZIO.succeed(SwaggerRoutes.routes)
          _                 <- Server.serve(
                                 app ++ nameRoutes ++ raceRoutes ++ characterRoutes ++ personalityRoutes ++ backgroundRoutes ++ classRoutes ++ spellRoutes ++ itemRoutes ++ titleRoutes ++ deityRoutes ++ languageRoutes ++ monsterRoutes ++ settlementNameRoutes ++ settlementRoutes ++ dungeonRoutes ++ hexMapRoutes ++ swaggerRoutes,
                               )
        } yield ()

        program.provide(
          Server.defaultWithPort(port),
          CharacterRepositoryLive.layer,
          NameRepositoryLive.layer,
          RaceRepositoryLive.layer,
          CharacterClassRepositoryLive.layer,
          PersonalityRepositoryLive.layer,
          BackgroundRepositoryLive.layer,
          SpellRepositoryLive.layer,
          ItemRepositoryLive.layer,
          TitleRepositoryLive.layer,
          DeityRepositoryLive.layer,
          LanguageRepositoryLive.layer,
          MonsterRepositoryLive.layer,
          NpcQualityRepositoryLive.layer,
          SettlementNameRepositoryLive.layer,
          NameServer.live,
          RaceServer.live,
          PersonalityServer.live,
          BackgroundServer.live,
          CharacterClassServer.live,
          SpellServer.live,
          ItemServer.live,
          TitleServer.live,
          DeityServer.live,
          LanguageServer.live,
          MonsterServer.live,
          NpcQualityServer.live,
          SettlementNameServer.live,
          SettlementServer.live,
          DungeonServer.live,
          HexMapServer.live,
          CharacterServer.live,
          CharacterRoute.live,
          NameRoute.live,
          RaceRoute.live,
          PersonalityRoute.live,
          BackgroundRoute.live,
          CharacterClassRoute.live,
          SpellRoute.live,
          ItemRoute.live,
          TitleRoute.live,
          DeityRoute.live,
          LanguageRoute.live,
          MonsterRoute.live,
          SettlementNameRoute.live,
          SettlementRoute.live,
          DungeonRoute.live,
          HexMapRoute.live,
        )
      }
      .orDie
}
