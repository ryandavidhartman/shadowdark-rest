import com.typesafe.config.ConfigFactory
import routes.{
  BackgroundRoute,
  CharacterClassRoute,
  CharacterRoute,
  ItemRoute,
  NameRoute,
  PersonalityRoute,
  RaceRoute,
  SpellRoute,
  TitleRoute,
}
import repositories.{
  BackgroundRepositoryLive,
  CharacterClassRepositoryLive,
  CharacterRepositoryLive,
  ItemRepositoryLive,
  NameRepositoryLive,
  PersonalityRepositoryLive,
  RaceRepositoryLive,
  SpellRepositoryLive,
  TitleRepositoryLive,
}
import servers.{
  BackgroundServer,
  CharacterClassServer,
  CharacterServer,
  ItemServer,
  NameServer,
  PersonalityServer,
  RaceServer,
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
          _                 <- Server.serve(
                                 app ++ nameRoutes ++ raceRoutes ++ characterRoutes ++ personalityRoutes ++ backgroundRoutes ++ classRoutes ++ spellRoutes ++ itemRoutes ++ titleRoutes,
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
          NameServer.live,
          RaceServer.live,
          PersonalityServer.live,
          BackgroundServer.live,
          CharacterClassServer.live,
          SpellServer.live,
          ItemServer.live,
          TitleServer.live,
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
        )
      }
      .orDie
}
