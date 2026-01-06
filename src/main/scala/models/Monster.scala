package models

import org.mongodb.scala.bson.ObjectId
import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder, JsonDecoder, JsonEncoder}

final case class MonsterSpecial(
  name: String,
  text: String,
)

object MonsterSpecial {
  implicit val jsonEncoder: JsonEncoder[MonsterSpecial] = DeriveJsonEncoder.gen[MonsterSpecial]
  implicit val jsonDecoder: JsonDecoder[MonsterSpecial] = DeriveJsonDecoder.gen[MonsterSpecial]
}

final case class MonsterStats(
  ac: String,
  hp: String,
  mv: String,
  str: String,
  dex: String,
  con: String,
  int: String,
  wis: String,
  cha: String,
)

object MonsterStats {
  implicit val jsonEncoder: JsonEncoder[MonsterStats] = DeriveJsonEncoder.gen[MonsterStats]
  implicit val jsonDecoder: JsonDecoder[MonsterStats] = DeriveJsonDecoder.gen[MonsterStats]
}

final case class Monster(
  _id: ObjectId,
  name: String,
  flavourText: String,
  attacks: String,
  stats: MonsterStats,
  alignment: String,
  level: String,
  specials: List[MonsterSpecial] = List.empty[MonsterSpecial],
)

object Monster {
  implicit val objectIdEncoder: JsonEncoder[ObjectId] = JsonEncoder.string.contramap[ObjectId](_.toHexString)
  implicit val objectIdDecoder: JsonDecoder[ObjectId] = JsonDecoder.string.mapOrFail { s =>
    if (s.matches("^[0-9a-fA-F]{24}$")) Right(new ObjectId(s))
    else Left(s"Invalid ObjectId: $s")
  }

  implicit val jsonEncoder: JsonEncoder[Monster] = DeriveJsonEncoder.gen[Monster]
  implicit val jsonDecoder: JsonDecoder[Monster] = DeriveJsonDecoder.gen[Monster]
}
