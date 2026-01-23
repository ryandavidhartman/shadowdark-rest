package routes

import models.HexMap
import servers.HexMapServer
import zio._
import zio.http._
import zio.json._

final case class HexNextRequest(
  map: HexMap,
  fromColumn: Int,
  fromRow: Int,
  direction: String,
)

object HexNextRequest {
  implicit val decoder: JsonDecoder[HexNextRequest] = DeriveJsonDecoder.gen[HexNextRequest]
}

final case class HexMapRoute(server: HexMapServer) {
  val routes: Routes[Any, Response] =
    Routes(
      Method.GET / "hexes" / "random" ->
        Handler
          .fromFunctionZIO[Request] { _ =>
            server.randomMap.map(map => Response.json(map.toJson))
          }
          .mapError(err => Response.internalServerError(err.getMessage)),
      Method.POST / "hexes" / "next" ->
        Handler
          .fromFunctionZIO[Request] { request =>
            request.body.asString.flatMap { body =>
              body.fromJson[HexNextRequest] match {
                case Left(error) =>
                  ZIO.succeed(Response.text(error).status(Status.BadRequest))
                case Right(payload) =>
                  server
                    .nextMap(payload.map, payload.fromColumn, payload.fromRow, payload.direction)
                    .map(map => Response.json(map.toJson))
              }
            }
          }
          .mapError(err => Response.internalServerError(err.getMessage)),
      Method.GET / "hexes" / "random.pdf" ->
        Handler
          .fromFunctionZIO[Request] { _ =>
            for {
              map <- server.randomMap
              pdf <- server.renderHexMapPdf(map)
            } yield Response(
              status = Status.Ok,
              headers = Headers(
                Header.Custom("Content-Type", "application/pdf"),
                Header.Custom("Content-Disposition", "inline; filename=\"random-hex-map.pdf\""),
              ),
              body = Body.fromChunk(Chunk.fromArray(pdf)),
            )
          }
          .mapError(err => Response.internalServerError(err.getMessage)),
    )
}

object HexMapRoute {
  val live: ZLayer[HexMapServer, Nothing, HexMapRoute] =
    ZLayer.fromFunction(HexMapRoute.apply _)
}
