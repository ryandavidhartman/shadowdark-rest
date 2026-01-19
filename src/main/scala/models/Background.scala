package models

import org.mongodb.scala.bson.ObjectId
import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder, JsonDecoder, JsonEncoder}

final case class BackgroundRange(
  min: Int,
  max: Int,
)

final case class Background(
  _id: ObjectId,
  name: String,
  range: BackgroundRange,
  possessions: String,
  details: String,
  poiKinds: Option[List[String]],
  poiNames: Option[List[String]],
)

object Background {
  implicit val objectIdEncoder: JsonEncoder[ObjectId] = JsonEncoder.string.contramap[ObjectId](_.toHexString)
  implicit val objectIdDecoder: JsonDecoder[ObjectId] = JsonDecoder.string.mapOrFail { s =>
    if (s.matches("^[0-9a-fA-F]{24}$")) Right(new ObjectId(s))
    else Left(s"Invalid ObjectId: $s")
  }

  implicit val rangeEncoder: JsonEncoder[BackgroundRange] = DeriveJsonEncoder.gen[BackgroundRange]
  implicit val rangeDecoder: JsonDecoder[BackgroundRange] = DeriveJsonDecoder.gen[BackgroundRange]

  implicit val jsonEncoder: JsonEncoder[Background] = DeriveJsonEncoder.gen[Background]
  implicit val jsonDecoder: JsonDecoder[Background] = DeriveJsonDecoder.gen[Background]
}
