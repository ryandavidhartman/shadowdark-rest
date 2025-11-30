package routes

import servers.CharacterServer
import zio._
import zio.http._
import zio.json._

final case class CharacterRoute(server: CharacterServer) {
  val routes: Routes[Any, Response] =
    Routes(
      Method.GET / "random-character" ->
        Handler
          .fromZIO(server.randomCharacter.map(character => Response.json(character.toJson)))
          .mapError(err => Response.internalServerError(err.getMessage))
    )
}

object CharacterRoute {
  val live: ZLayer[CharacterServer, Nothing, CharacterRoute] =
    ZLayer.fromFunction(CharacterRoute.apply _)
}
