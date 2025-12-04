package routes

import models.Name
import org.mongodb.scala.bson.ObjectId
import servers.NameServer
import zio._
import zio.http._
import zio.json._

final case class NameRoute(server: NameServer) {

  private final case class NameCreate(
    name: String,
    race: String,
    gender: Option[String],
    firstName: Option[Boolean],
    lastName: Option[Boolean],
  )
  private object NameCreate {
    implicit val decoder: JsonDecoder[NameCreate] = DeriveJsonDecoder.gen[NameCreate]
  }

  private def decodeName(req: Request): IO[Response, Name] =
    for {
      body <- req.body.asString.orElseFail(Response.status(Status.BadRequest))
      dto  <- ZIO
               .fromEither(body.fromJson[NameCreate])
               .mapError(err => Response.text(err).status(Status.BadRequest))
    } yield Name(
      _id = new ObjectId(),
      name = dto.name,
      race = dto.race,
      gender = dto.gender,
      firstName = dto.firstName,
      lastName = dto.lastName,
    )

  val routes: Routes[Any, Response] =
    Routes(
      Method.GET / "names" ->
        Handler
          .fromZIO(server.getNames.map(names => Response.json(names.toJson)))
          .mapError(err => Response.internalServerError(err.getMessage)),
      Method.POST / "names" ->
        Handler
          .fromFunctionZIO[Request] { req =>
            for {
              name   <- decodeName(req)
              saved  <- server.createName(name)
            } yield Response.json(saved.toJson).status(Status.Created)
          }
          .mapError {
            case resp: Response => resp
            case err            => Response.internalServerError(err.toString)
          },
    )
}

object NameRoute {
  val live: ZLayer[NameServer, Nothing, NameRoute] =
    ZLayer.fromFunction(NameRoute.apply _)
}
