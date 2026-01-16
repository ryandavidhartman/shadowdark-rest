package routes

import servers.SettlementNameServer
import zio._
import zio.http._
import zio.json._

final case class SettlementNameRoute(server: SettlementNameServer) {
  val routes: Routes[Any, Response] =
    Routes(
      Method.GET / "settlement-names" ->
        Handler
          .fromZIO(server.getSettlementNames.map(names => Response.json(names.toJson)))
          .mapError(err => Response.internalServerError(err.getMessage)),
    )
}

object SettlementNameRoute {
  val live: ZLayer[SettlementNameServer, Nothing, SettlementNameRoute] =
    ZLayer.fromFunction(SettlementNameRoute.apply _)
}
