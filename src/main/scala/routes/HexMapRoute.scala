package routes

import models.HexMap
import servers.HexMapServer
import zio._
import zio.http._
import zio.json._

final case class HexNextRequest(
  map: HexMap,
  direction: String,
)

object HexNextRequest {
  implicit val decoder: JsonDecoder[HexNextRequest] = DeriveJsonDecoder.gen[HexNextRequest]
}

final case class HexRenderRequest(
  map: HexMap,
  `type`: String,
)

object HexRenderRequest {
  implicit val decoder: JsonDecoder[HexRenderRequest] = DeriveJsonDecoder.gen[HexRenderRequest]
}

final case class HexError(
  error: String,
  details: Option[String] = None,
  allowed: Option[List[String]] = None,
)

object HexError {
  implicit val encoder: JsonEncoder[HexError] = DeriveJsonEncoder.gen[HexError]
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
                  val payload = HexError(error = "Invalid next-hex payload", details = Some(error))
                  ZIO.succeed(Response.json(payload.toJson).status(Status.BadRequest))
                case Right(payload) =>
                  server
                    .nextMap(payload.map, payload.direction)
                    .map(map => Response.json(map.toJson))
              }
            }
          }
          .mapError(err => Response.internalServerError(err.getMessage)),
      Method.POST / "hexes" / "render" ->
        Handler
          .fromFunctionZIO[Request] { request =>
            request.body.asString.flatMap { body =>
              body.fromJson[HexRenderRequest] match {
                case Left(error) =>
                  val payload = HexError(error = "Invalid render payload", details = Some(error))
                  ZIO.succeed(Response.json(payload.toJson).status(Status.BadRequest))
                case Right(payload) =>
                  payload.`type`.trim.toLowerCase match {
                    case "pdf" =>
                      server
                        .renderHexMapPdf(payload.map)
                        .map { pdf =>
                          Response(
                            status = Status.Ok,
                            headers = Headers(
                              Header.Custom("Content-Type", "application/pdf"),
                              Header.Custom("Content-Disposition", "inline; filename=\"hex-map.pdf\""),
                            ),
                            body = Body.fromChunk(Chunk.fromArray(pdf)),
                          )
                        }
                    case "png" =>
                      server
                        .renderHexMapPng(payload.map)
                        .map { png =>
                          Response(
                            status = Status.Ok,
                            headers = Headers(
                              Header.Custom("Content-Type", "image/png"),
                              Header.Custom("Content-Disposition", "inline; filename=\"hex-map.png\""),
                            ),
                            body = Body.fromChunk(Chunk.fromArray(png)),
                          )
                        }
                    case "json" =>
                      ZIO.succeed(Response.json(payload.map.toJson))
                    case other =>
                      val payload = HexError(
                        error = s"Unsupported render type: $other",
                        allowed = Some(List("pdf", "png", "json")),
                      )
                      ZIO.succeed(Response.json(payload.toJson).status(Status.BadRequest))
                  }
              }
            }
          }
          .mapError(err => Response.internalServerError(err.getMessage)),
    )
}

object HexMapRoute {
  val live: ZLayer[HexMapServer, Nothing, HexMapRoute] =
    ZLayer.fromFunction(HexMapRoute.apply _)
}
