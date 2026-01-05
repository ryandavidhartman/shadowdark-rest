package routes

import servers.DeityServer
import zio._
import zio.http._
import zio.json._

final case class DeityRoute(server: DeityServer) {
  val routes: Routes[Any, Response] =
    Routes(
      Method.GET / "deities" ->
        Handler
          .fromZIO(server.getDeities.map(deities => Response.json(deities.toJson)))
          .mapError(err => Response.internalServerError(err.getMessage)),
    )
}

object DeityRoute {
  val live: ZLayer[DeityServer, Nothing, DeityRoute] =
    ZLayer.fromFunction(DeityRoute.apply _)
}
