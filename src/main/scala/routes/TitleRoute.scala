package routes

import servers.TitleServer
import zio._
import zio.http._
import zio.json._

final case class TitleRoute(server: TitleServer) {
  val routes: Routes[Any, Response] =
    Routes(
      Method.GET / "titles" ->
        Handler
          .fromZIO(server.getTitles.map(titles => Response.json(titles.toJson)))
          .mapError(err => Response.internalServerError(err.getMessage))
    )
}

object TitleRoute {
  val live: ZLayer[TitleServer, Nothing, TitleRoute] =
    ZLayer.fromFunction(TitleRoute.apply _)
}
