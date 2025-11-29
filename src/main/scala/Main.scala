import com.typesafe.config.ConfigFactory
import routes.{NameRoute, RaceRoute}
import repositories.{NameRepositoryLive, RaceRepositoryLive}
import servers.{NameServer, RaceServer}
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
          nameRoutes <- ZIO.serviceWith[NameRoute](_.routes)
          raceRoutes <- ZIO.serviceWith[RaceRoute](_.routes)
          _          <- Server.serve(app ++ nameRoutes ++ raceRoutes)
        } yield ()

        program.provide(
          Server.defaultWithPort(port),
          NameRepositoryLive.layer,
          RaceRepositoryLive.layer,
          NameServer.live,
          RaceServer.live,
          NameRoute.live,
          RaceRoute.live
        )
      }
      .orDie
}
