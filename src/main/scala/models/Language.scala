package models

import zio.json.{DeriveJsonEncoder, JsonEncoder}

final case class Language(common: List[String], rare: List[String])

object Language {
  val default: Language = Language(
    common = List(
      "Dwarvish",
      "Elvish",
      "Giant",
      "Gnomish",
      "Goblin",
      "Merran",
      "Orcish",
      "Reptilian",
      "Sylvan",
      "Thanian",
    ),
    rare = List(
      "Celestial",
      "Diabolic",
      "Draconic",
      "Primordial",
      "Thieves' Cant",
      "Ancient",
    ),
  )

  implicit val jsonEncoder: JsonEncoder[Language] = DeriveJsonEncoder.gen[Language]
}
