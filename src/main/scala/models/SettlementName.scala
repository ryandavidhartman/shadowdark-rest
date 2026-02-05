package models

import org.mongodb.scala.bson.ObjectId
import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder, JsonDecoder, JsonEncoder}

final case class SettlementName(
    _id: ObjectId,
    name: String,
    settlementType: String
)

object SettlementName {
  implicit val objectIdEncoder: JsonEncoder[ObjectId] = JsonEncoder.string.contramap[ObjectId](_.toHexString)
  implicit val objectIdDecoder: JsonDecoder[ObjectId] = JsonDecoder.string.mapOrFail { s =>
    if (s.matches("^[0-9a-fA-F]{24}$")) Right(new ObjectId(s))
    else Left(s"Invalid ObjectId: $s")
  }

  implicit val jsonEncoder: JsonEncoder[SettlementName] = DeriveJsonEncoder.gen[SettlementName]
  implicit val jsonDecoder: JsonDecoder[SettlementName] = DeriveJsonDecoder.gen[SettlementName]
}
