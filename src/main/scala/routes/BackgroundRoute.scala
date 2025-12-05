package routes

import servers.BackgroundServer
import zio._
import zio.http._
import zio.json._

final case class BackgroundRoute(server: BackgroundServer) {
  val routes: Routes[Any, Response] =
    Routes(
      Method.GET / "backgrounds" ->
        Handler
          .fromZIO(server.getBackgrounds.map(bgs => Response.json(bgs.toJson)))
          .mapError(err => Response.internalServerError(err.getMessage)),
    )
}

object BackgroundRoute {
  val live: ZLayer[BackgroundServer, Nothing, BackgroundRoute] =
    ZLayer.fromFunction(BackgroundRoute.apply _)
}
