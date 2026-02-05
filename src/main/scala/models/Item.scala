package models

import org.mongodb.scala.bson.ObjectId
import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder, JsonDecoder, JsonEncoder}

final case class Item(
    _id: ObjectId,
    name: String,
    itemType: Option[String] = None,
    description: Option[String] = None,
    cost: Option[String] = None,
    nonidentified: Option[String] = None,
    slots: Int = 1,
    magical: Option[Boolean] = None,
    ac: Option[Int] = None,
    count: Option[Int] = None,
    nodex: Option[Int] = None,
    attackType: Option[String] = None,
    damage: Option[String] = None,
    damageType: Option[String] = None,
    finesse: Option[Boolean] = None,
    itemAttackBonus: Option[Int] = None,
    defenseBonus: Option[Int] = None,
    loading: Option[Boolean] = None,
    loadingFullRound: Option[Boolean] = None,
    range: Option[String] = None,
    thrown: Option[Boolean] = None,
    twoHanded: Option[Boolean] = None,
    versatile: Option[Boolean] = None,
    versatileDamage: Option[String] = None
)

object Item {
  implicit val objectIdEncoder: JsonEncoder[ObjectId] = JsonEncoder.string.contramap[ObjectId](_.toHexString)
  implicit val objectIdDecoder: JsonDecoder[ObjectId] = JsonDecoder.string.mapOrFail { s =>
    if (s.matches("^[0-9a-fA-F]{24}$")) Right(new ObjectId(s))
    else Left(s"Invalid ObjectId: $s")
  }

  implicit val jsonEncoder: JsonEncoder[Item] = DeriveJsonEncoder.gen[Item]
  implicit val jsonDecoder: JsonDecoder[Item] = DeriveJsonDecoder.gen[Item]
}
