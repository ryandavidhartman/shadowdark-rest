import com.typesafe.config.ConfigFactory
import models.Name
import names.NameRoute
import repositories.{NameRepositoryLive, NameRepository}
import servers.NameServer
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
          routes <- ZIO.serviceWith[NameRoute](_.routes)
          _      <- Server.serve(app ++ routes)
        } yield ()

        program.provide(
          Server.defaultWithPort(port),
          NameRepositoryLive.layer,
          NameServer.live,
          NameRoute.live
        )
      }
      .orDie
}
