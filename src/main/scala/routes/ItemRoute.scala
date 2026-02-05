package routes

import servers.ItemServer
import zio._
import zio.http._
import zio.json._

final case class ItemRoute(server: ItemServer) {
  val routes: Routes[Any, Response] =
    Routes(
      Method.GET / "items" ->
        Handler
          .fromZIO(server.getItems.map(items => Response.json(items.toJson)))
          .mapError(err => Response.internalServerError(err.getMessage))
    )
}

object ItemRoute {
  val live: ZLayer[ItemServer, Nothing, ItemRoute] =
    ZLayer.fromFunction(ItemRoute.apply _)
}
