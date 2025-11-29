package routes

import servers.RaceServer
import zio._
import zio.http._
import zio.json._

final case class RaceRoute(server: RaceServer) {
  val routes: Routes[Any, Response] =
    Routes(
      Method.GET / "races" ->
        Handler
          .fromZIO(server.getRaces.map(races => Response.json(races.toJson)))
          .mapError(err => Response.internalServerError(err.getMessage))
    )
}

object RaceRoute {
  val live: ZLayer[RaceServer, Nothing, RaceRoute] =
    ZLayer.fromFunction(RaceRoute.apply _)
}
