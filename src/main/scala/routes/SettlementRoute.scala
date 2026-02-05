package routes

import servers.SettlementServer
import zio._
import zio.http._
import zio.json._

final case class SettlementRoute(server: SettlementServer) {
  val routes: Routes[Any, Response] =
    Routes(
      Method.GET / "settlements" / "random" ->
        Handler
          .fromFunctionZIO[Request] { _ =>
            server.randomSettlement.map(settlement => Response.json(settlement.toJson))
          }
          .mapError(err => Response.internalServerError(err.getMessage)),
      Method.GET / "settlements" / "random.pdf" ->
        Handler
          .fromFunctionZIO[Request] { _ =>
            for {
              settlement <- server.randomSettlement
              pdf <- server.renderSettlementPdf(settlement)
            } yield Response(
              status = Status.Ok,
              headers = Headers(
                Header.Custom("Content-Type", "application/pdf"),
                Header.Custom("Content-Disposition", "inline; filename=\"random-settlement.pdf\"")
              ),
              body = Body.fromChunk(Chunk.fromArray(pdf))
            )
          }
          .mapError(err => Response.internalServerError(err.getMessage))
    )
}

object SettlementRoute {
  val live: ZLayer[SettlementServer, Nothing, SettlementRoute] =
    ZLayer.fromFunction(SettlementRoute.apply _)
}
