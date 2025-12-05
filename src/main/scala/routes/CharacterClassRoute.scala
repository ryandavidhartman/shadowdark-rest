package routes

import servers.CharacterClassServer
import zio._
import zio.http._
import zio.json._

final case class CharacterClassRoute(server: CharacterClassServer) {
  val routes: Routes[Any, Response] =
    Routes(
      Method.GET / "classes" ->
        Handler
          .fromZIO(server.getClasses.map(cls => Response.json(cls.toJson)))
          .mapError(err => Response.internalServerError(err.getMessage)),
    )
}

object CharacterClassRoute {
  val live: ZLayer[CharacterClassServer, Nothing, CharacterClassRoute] =
    ZLayer.fromFunction(CharacterClassRoute.apply _)
}
