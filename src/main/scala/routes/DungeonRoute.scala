package routes

import servers.DungeonServer
import zio._
import zio.http._
import zio.json._

final case class DungeonRoute(server: DungeonServer) {
  val routes: Routes[Any, Response] =
    Routes(
      Method.GET / "dungeons" / "random" ->
        Handler
          .fromFunctionZIO[Request] { _ =>
            server.randomDungeon.map(dungeon => Response.json(dungeon.toJson))
          }
          .mapError(err => Response.internalServerError(err.getMessage)),
      Method.GET / "dungeons" / "random.pdf" ->
        Handler
          .fromFunctionZIO[Request] { _ =>
            for {
              dungeon <- server.randomDungeon
              pdf <- server.renderDungeonPdf(dungeon)
            } yield Response(
              status = Status.Ok,
              headers = Headers(
                Header.Custom("Content-Type", "application/pdf"),
                Header.Custom("Content-Disposition", "inline; filename=\"random-dungeon.pdf\"")
              ),
              body = Body.fromChunk(Chunk.fromArray(pdf))
            )
          }
          .mapError(err => Response.internalServerError(err.getMessage))
    )
}

object DungeonRoute {
  val live: ZLayer[DungeonServer, Nothing, DungeonRoute] =
    ZLayer.fromFunction(DungeonRoute.apply _)
}
