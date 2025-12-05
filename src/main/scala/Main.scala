import com.typesafe.config.ConfigFactory
import routes.{CharacterRoute, NameRoute, PersonalityRoute, RaceRoute}
import repositories.{CharacterClassRepositoryLive, CharacterRepositoryLive, NameRepositoryLive, PersonalityRepositoryLive, RaceRepositoryLive}
import servers.{CharacterServer, NameServer, PersonalityServer, RaceServer}
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
          _                 <- Server.serve(app ++ nameRoutes ++ raceRoutes ++ characterRoutes ++ personalityRoutes)
        } yield ()

        program.provide(
          Server.defaultWithPort(port),
          CharacterRepositoryLive.layer,
          NameRepositoryLive.layer,
          RaceRepositoryLive.layer,
          CharacterClassRepositoryLive.layer,
          PersonalityRepositoryLive.layer,
          NameServer.live,
          RaceServer.live,
          PersonalityServer.live,
          CharacterServer.live,
          CharacterRoute.live,
          NameRoute.live,
          RaceRoute.live,
          PersonalityRoute.live
        )
      }
      .orDie
}
