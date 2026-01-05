package routes

import servers.LanguageServer
import zio._
import zio.http._
import zio.json._

final case class LanguageRoute(server: LanguageServer) {
  val routes: Routes[Any, Response] =
    Routes(
      Method.GET / "languages" ->
        Handler
          .fromZIO(server.getLanguages.map(languages => Response.json(languages.toJson)))
          .mapError(err => Response.internalServerError(err.getMessage)),
    )
}

object LanguageRoute {
  val live: ZLayer[LanguageServer, Nothing, LanguageRoute] =
    ZLayer.fromFunction(LanguageRoute.apply _)
}
