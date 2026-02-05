package models

import org.mongodb.scala.bson.ObjectId
import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder, JsonDecoder, JsonEncoder}

sealed trait CharacterClass {
  def name: String
  def weapons: List[String]
  def armor: List[String]
  def hitPointsPerLevel: String
  def languages: Option[LanguageBenefit]
  def abilityPriority: List[String]
  def features: List[ClassFeature]
  def spellcasting: Option[Spellcasting]
  def talents: List[Talent]
  def titles: List[ClassTitle]
}

final case class LanguageBenefit(
    choices: List[String],
    choose: Int,
    extraCommon: Int = 0,
    extraRare: Int = 0,
    notes: Option[String] = None
)

final case class ClassFeature(
    name: String,
    description: String
)

final case class Spellcasting(
    spellList: String,
    initialKnown: Map[String, Int],
    progression: List[SpellProgression],
    notes: List[String] = Nil
)

final case class SpellProgression(
    level: Int,
    tiers: SpellTiers
)

final case class SpellTiers(
    tier1: Int,
    tier2: Int,
    tier3: Int,
    tier4: Int,
    tier5: Int
)

final case class Talent(
    range: DiceRange,
    effect: String
)

final case class DiceRange(
    min: Int,
    max: Int
) {
  def label: String =
    if (min == max) min.toString else s"$min-$max"
}

final case class ClassTitle(
    levels: LevelRange,
    lawful: String,
    chaotic: String,
    neutral: String
)

final case class LevelRange(
    min: Int,
    max: Int
) {
  def label: String =
    if (min == max) min.toString else s"$min-$max"
}

final case class StoredCharacterClass(
    _id: ObjectId,
    name: String,
    weapons: List[String],
    armor: List[String],
    hitPointsPerLevel: String,
    languages: Option[LanguageBenefit],
    abilityPriority: List[String] = List.empty,
    features: List[ClassFeature],
    spellcasting: Option[Spellcasting],
    talents: List[Talent],
    titles: List[ClassTitle]
) extends CharacterClass

object StoredCharacterClass {
  def fromClass(idHex: String, characterClass: CharacterClass): StoredCharacterClass =
    StoredCharacterClass(
      _id = new ObjectId(idHex),
      name = characterClass.name,
      weapons = characterClass.weapons,
      armor = characterClass.armor,
      hitPointsPerLevel = characterClass.hitPointsPerLevel,
      languages = characterClass.languages,
      abilityPriority = characterClass.abilityPriority,
      features = characterClass.features,
      spellcasting = characterClass.spellcasting,
      talents = characterClass.talents,
      titles = characterClass.titles
    )

  implicit val objectIdEncoder: JsonEncoder[ObjectId] = JsonEncoder.string.contramap[ObjectId](_.toHexString)
  implicit val objectIdDecoder: JsonDecoder[ObjectId] = JsonDecoder.string.mapOrFail { s =>
    if (s.matches("^[0-9a-fA-F]{24}$")) Right(new ObjectId(s))
    else Left(s"Invalid ObjectId: $s")
  }
  implicit val languageBenefitEncoder: JsonEncoder[LanguageBenefit] =
    DeriveJsonEncoder.gen[LanguageBenefit]
  implicit val languageBenefitDecoder: JsonDecoder[LanguageBenefit] =
    DeriveJsonDecoder.gen[LanguageBenefit]
  implicit val classFeatureEncoder: JsonEncoder[ClassFeature] =
    DeriveJsonEncoder.gen[ClassFeature]
  implicit val classFeatureDecoder: JsonDecoder[ClassFeature] =
    DeriveJsonDecoder.gen[ClassFeature]
  implicit val spellTiersEncoder: JsonEncoder[SpellTiers] =
    DeriveJsonEncoder.gen[SpellTiers]
  implicit val spellTiersDecoder: JsonDecoder[SpellTiers] =
    DeriveJsonDecoder.gen[SpellTiers]
  implicit val spellProgressionEncoder: JsonEncoder[SpellProgression] =
    DeriveJsonEncoder.gen[SpellProgression]
  implicit val spellProgressionDecoder: JsonDecoder[SpellProgression] =
    DeriveJsonDecoder.gen[SpellProgression]
  implicit val spellcastingEncoder: JsonEncoder[Spellcasting] =
    DeriveJsonEncoder.gen[Spellcasting]
  implicit val spellcastingDecoder: JsonDecoder[Spellcasting] =
    DeriveJsonDecoder.gen[Spellcasting]
  implicit val diceRangeEncoder: JsonEncoder[DiceRange] =
    DeriveJsonEncoder.gen[DiceRange]
  implicit val diceRangeDecoder: JsonDecoder[DiceRange] =
    DeriveJsonDecoder.gen[DiceRange]
  implicit val talentEncoder: JsonEncoder[Talent] =
    DeriveJsonEncoder.gen[Talent]
  implicit val talentDecoder: JsonDecoder[Talent] =
    DeriveJsonDecoder.gen[Talent]
  implicit val levelRangeEncoder: JsonEncoder[LevelRange] =
    DeriveJsonEncoder.gen[LevelRange]
  implicit val levelRangeDecoder: JsonDecoder[LevelRange] =
    DeriveJsonDecoder.gen[LevelRange]
  implicit val classTitleEncoder: JsonEncoder[ClassTitle] =
    DeriveJsonEncoder.gen[ClassTitle]
  implicit val classTitleDecoder: JsonDecoder[ClassTitle] =
    DeriveJsonDecoder.gen[ClassTitle]
  implicit val storedCharacterClassEncoder: JsonEncoder[StoredCharacterClass] =
    DeriveJsonEncoder.gen[StoredCharacterClass]
  implicit val storedCharacterClassDecoder: JsonDecoder[StoredCharacterClass] =
    DeriveJsonDecoder.gen[StoredCharacterClass]
}
