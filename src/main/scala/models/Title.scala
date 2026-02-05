package models

import org.mongodb.scala.bson.ObjectId
import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder, JsonDecoder, JsonEncoder}

final case class Title(
    _id: ObjectId,
    characterClass: String,
    alignment: String,
    minLevel: Int,
    maxLevel: Int,
    title: String
)

object Title {
  implicit val objectIdEncoder: JsonEncoder[ObjectId] = JsonEncoder.string.contramap[ObjectId](_.toHexString)
  implicit val objectIdDecoder: JsonDecoder[ObjectId] = JsonDecoder.string.mapOrFail { s =>
    if (s.matches("^[0-9a-fA-F]{24}$")) Right(new ObjectId(s))
    else Left(s"Invalid ObjectId: $s")
  }

  implicit val jsonEncoder: JsonEncoder[Title] = DeriveJsonEncoder.gen[Title]
  implicit val jsonDecoder: JsonDecoder[Title] = DeriveJsonDecoder.gen[Title]
}
