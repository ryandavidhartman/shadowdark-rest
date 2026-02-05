package routes

import servers.MonsterServer
import zio._
import zio.http._
import zio.json._

final case class MonsterRoute(server: MonsterServer) {
  val routes: Routes[Any, Response] =
    Routes(
      Method.GET / "monsters" ->
        Handler
          .fromZIO(server.getMonsters.map(monsters => Response.json(monsters.toJson)))
          .mapError(err => Response.internalServerError(err.getMessage))
    )
}

object MonsterRoute {
  val live: ZLayer[MonsterServer, Nothing, MonsterRoute] =
    ZLayer.fromFunction(MonsterRoute.apply _)
}
