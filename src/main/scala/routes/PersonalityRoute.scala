package routes

import models.Personality
import org.mongodb.scala.bson.ObjectId
import servers.PersonalityServer
import zio._
import zio.http._
import zio.json._

private[routes] final case class PersonalityCreate(
  name: String,
  alignment: String,
)
private[routes] object PersonalityCreate {
  implicit val decoder: JsonDecoder[PersonalityCreate] = DeriveJsonDecoder.gen[PersonalityCreate]
}

final case class PersonalityRoute(server: PersonalityServer) {

  private def decodePersonality(req: Request): IO[Response, Personality] =
    for {
      body <- req.body.asString.orElseFail(Response.status(Status.BadRequest))
      dto  <- ZIO
               .fromEither(body.fromJson[PersonalityCreate])
               .mapError(err => Response.text(err).status(Status.BadRequest))
    } yield Personality(
      _id = new ObjectId(),
      name = dto.name,
      alignment = dto.alignment,
    )

  val routes: Routes[Any, Response] =
    Routes(
      Method.GET / "personalities" ->
        Handler
          .fromZIO(server.getPersonalities.map(personalities => Response.json(personalities.toJson)))
          .mapError(err => Response.internalServerError(err.getMessage)),
      Method.POST / "personalities" ->
        Handler
          .fromFunctionZIO[Request] { req =>
            for {
              personality <- decodePersonality(req)
              saved       <- server.createPersonality(personality)
            } yield Response.json(saved.toJson).status(Status.Created)
          }
          .mapError {
            case resp: Response => resp
            case err            => Response.internalServerError(err.toString)
          },
    )
}

object PersonalityRoute {
  val live: ZLayer[PersonalityServer, Nothing, PersonalityRoute] =
    ZLayer.fromFunction(PersonalityRoute.apply _)
}
