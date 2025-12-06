package models

import org.mongodb.scala.bson.ObjectId
import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder, JsonDecoder, JsonEncoder}

final case class Spell(
  _id: ObjectId,
  name: String,
  tier: Int,
  castingAttribute: String,
  spellType: Option[String] = None,
  range: Option[String] = None,
  duration: Option[String] = None,
  dc: Option[Int] = None,
  description: String,
  damage: Option[String] = None,
  damageType: Option[String] = None,
  healing: Option[String] = None,
  levelScaling: Option[String] = None,
  multiplier: Option[Int] = None,
  opposed: Option[Int] = None,
  opposedDc: Option[Int] = None,
  opposedAbility: Option[String] = None,
)

object Spell {
  implicit val objectIdEncoder: JsonEncoder[ObjectId] = JsonEncoder.string.contramap[ObjectId](_.toHexString)
  implicit val objectIdDecoder: JsonDecoder[ObjectId] = JsonDecoder.string.mapOrFail { s =>
    if (s.matches("^[0-9a-fA-F]{24}$")) Right(new ObjectId(s))
    else Left(s"Invalid ObjectId: $s")
  }

  implicit val jsonEncoder: JsonEncoder[Spell] = DeriveJsonEncoder.gen[Spell]
  implicit val jsonDecoder: JsonDecoder[Spell] = DeriveJsonDecoder.gen[Spell]
}
