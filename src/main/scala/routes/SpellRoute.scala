package routes

import servers.SpellServer
import zio._
import zio.http._
import zio.json._

final case class SpellRoute(server: SpellServer) {
  val routes: Routes[Any, Response] =
    Routes(
      Method.GET / "spells" ->
        Handler
          .fromZIO(server.getSpells.map(spells => Response.json(spells.toJson)))
          .mapError(err => Response.internalServerError(err.getMessage)),
    )
}

object SpellRoute {
  val live: ZLayer[SpellServer, Nothing, SpellRoute] =
    ZLayer.fromFunction(SpellRoute.apply _)
}
