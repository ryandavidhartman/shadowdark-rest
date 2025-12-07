package models

import org.mongodb.scala.bson.ObjectId
import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder, JsonDecoder, JsonEncoder}
import models.StoredCharacterClass._

final case class AbilityScore(score: Int) {
  val modifier: Int = score match {
    case i if i <= 3 => -4
    case i if i >= 4 && i <= 5 => -3
    case i if i >= 6 && i <= 7 => -2
    case i if i >= 8 && i <= 9 => -1
    case i if i >= 10 && i <= 11 => 0
    case i if i >= 12 && i <= 13 => 1
    case i if i >= 14 && i <= 15 => 2
    case i if i >= 16 && i <= 17 => 3
    case i if i >= 18 => 4
  }
}

final case class AbilityScores(
  strength: AbilityScore,
  dexterity: AbilityScore,
  constitution: AbilityScore,
  intelligence: AbilityScore,
  wisdom: AbilityScore,
  charisma: AbilityScore,
)

final case class Character(
  _id: ObjectId,
  name: String,
  ancestry: Option[String] = None,
  characterClass: Option[String] = None,
  level: Option[Int] = None,
  xp: Option[Int] = None,
  title: Option[String] = None,
  alignment: Option[String] = None,
  background: Option[String] = None,
  deity: Option[String] = None,
  abilities: AbilityScores,
  hitPoints: Int,
  armorClass: Int,
  features: List[ClassFeature] = List.empty,
  talents: List[String] = List.empty,
  spells: List[String] = List.empty,
  attacks: List[String] = List.empty,
  gear: List[String] = List.empty,
  languages: Set[String] = Set.empty,
  personalities: List[String] = List.empty,
  gender: Option[String] = None,
  goldPieces: Int = 0,
  silverPieces: Int = 0,
  copperPieces: Int = 0,
  freeToCarry: List[String] = List.empty,
)

object Character {
  implicit val objectIdEncoder: JsonEncoder[ObjectId] = JsonEncoder.string.contramap[ObjectId](_.toHexString)
  implicit val objectIdDecoder: JsonDecoder[ObjectId] = JsonDecoder.string.mapOrFail { s =>
    if (s.matches("^[0-9a-fA-F]{24}$")) Right(new ObjectId(s))
    else Left(s"Invalid ObjectId: $s")
  }

  implicit val abilityScoreEncoder: JsonEncoder[AbilityScore] =
    JsonEncoder[Map[String, Int]].contramap { abilityScore =>
      Map(
        "score"    -> abilityScore.score,
        "modifier" -> abilityScore.modifier,
      )
    }

  implicit val abilityScoreDecoder: JsonDecoder[AbilityScore] =
    DeriveJsonDecoder.gen[AbilityScore]

  implicit val abilitiesEncoder: JsonEncoder[AbilityScores] = DeriveJsonEncoder.gen[AbilityScores]
  implicit val abilitiesDecoder: JsonDecoder[AbilityScores] = DeriveJsonDecoder.gen[AbilityScores]

  implicit val jsonEncoder: JsonEncoder[Character] = DeriveJsonEncoder.gen[Character]
  implicit val jsonDecoder: JsonDecoder[Character] = DeriveJsonDecoder.gen[Character]
}
