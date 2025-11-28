package models

import org.mongodb.scala.bson.ObjectId
import zio.json.{DeriveJsonEncoder, JsonEncoder, JsonFieldEncoder, JsonFieldDecoder, JsonDecoder}

// Represents a name entry aligned with the provided schema.
final case class Name(
  _id: ObjectId,
  name: String,
  race: String,
  gender: Option[String] = None,
  firstName: Option[Boolean] = None,
  lastName: Option[Boolean] = None
)

object Name {
  implicit val objectIdEncoder: JsonEncoder[ObjectId] = JsonEncoder.string.contramap[ObjectId](_.toHexString)
  implicit val objectIdDecoder: JsonDecoder[ObjectId] = JsonDecoder.string.mapOrFail { s =>
    if (s.matches("^[0-9a-fA-F]{24}$")) Right(new ObjectId(s))
    else Left(s"Invalid ObjectId: $s")
  }
  implicit val jsonEncoder: JsonEncoder[Name] = DeriveJsonEncoder.gen[Name]
}
