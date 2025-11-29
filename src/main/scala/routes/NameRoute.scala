package routes

import servers.NameServer
import zio._
import zio.http._
import zio.json._

final case class NameRoute(server: NameServer) {
  val routes: Routes[Any, Response] =
    Routes(
      Method.GET / "names" ->
        Handler
          .fromZIO(server.getNames.map(names => Response.json(names.toJson)))
          .mapError(err => Response.internalServerError(err.getMessage))
    )
}

object NameRoute {
  val live: ZLayer[NameServer, Nothing, NameRoute] =
    ZLayer.fromFunction(NameRoute.apply _)
}
