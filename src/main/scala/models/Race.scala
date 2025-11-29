package models

import org.mongodb.scala.bson.ObjectId
import zio.json.{DeriveJsonEncoder, JsonDecoder, JsonEncoder}

// Represents a race entry aligned with the provided schema.
final case class Race(
  _id: ObjectId,
  race: String,
  ability: RaceAbility,
  languages: List[String],
  chance: Double,
)

final case class RaceAbility(
  name: String,
  description: String,
)

object Race {
  implicit val objectIdEncoder: JsonEncoder[ObjectId] = JsonEncoder.string.contramap[ObjectId](_.toHexString)
  implicit val objectIdDecoder: JsonDecoder[ObjectId] = JsonDecoder.string.mapOrFail { s =>
    if (s.matches("^[0-9a-fA-F]{24}$")) Right(new ObjectId(s))
    else Left(s"Invalid ObjectId: $s")
  }

  implicit val abilityEncoder: JsonEncoder[RaceAbility] = DeriveJsonEncoder.gen[RaceAbility]
  implicit val jsonEncoder: JsonEncoder[Race]           = DeriveJsonEncoder.gen[Race]
}
