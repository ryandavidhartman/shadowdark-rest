package models

import org.mongodb.scala.bson.ObjectId
import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder, JsonDecoder, JsonEncoder}

final case class NpcQuality(
  _id: ObjectId,
  category: String,
  value: String,
)

object NpcQuality {
  implicit val objectIdEncoder: JsonEncoder[ObjectId] = JsonEncoder.string.contramap[ObjectId](_.toHexString)
  implicit val objectIdDecoder: JsonDecoder[ObjectId] = JsonDecoder.string.mapOrFail { s =>
    if (s.matches("^[0-9a-fA-F]{24}$")) Right(new ObjectId(s))
    else Left(s"Invalid ObjectId: $s")
  }

  implicit val jsonEncoder: JsonEncoder[NpcQuality] = DeriveJsonEncoder.gen[NpcQuality]
  implicit val jsonDecoder: JsonDecoder[NpcQuality] = DeriveJsonDecoder.gen[NpcQuality]
}
