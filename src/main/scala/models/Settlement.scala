package models

import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder, JsonDecoder, JsonEncoder}

final case class Point(x: Int, y: Int)

final case class SettlementType(
  name: String,
  diceCount: Int,
  dieSides: Int,
)

final case class SettlementLayout(
  width: Int,
  height: Int,
  gridSize: Int,
  outline: List[Point],
  seed: Long,
)

final case class Drink(
  name: String,
  cost: String,
  effect: String,
)

final case class Food(
  name: String,
  cost: String,
)

final case class Tavern(
  name: String,
  knownFor: String,
  drinks: List[Drink],
  food: List[Food],
)

final case class Npc(
  name: String,
  ancestry: String,
  age: String,
  alignment: String,
  wealth: String,
  appearance: String,
  mannerism: String,
  secret: String,
  background: Option[String],
  personality: Option[String],
)

final case class Shop(
  name: String,
  shopType: String,
  knownFor: String,
  interestingCustomer: String,
)

final case class PointOfInterest(
  id: Int,
  name: String,
  kind: String,
  location: Point,
  tavern: Option[Tavern],
  shop: Option[Shop],
  npc: Option[Npc],
  buildingId: Option[Int],
)

final case class Building(
  id: Int,
  footprint: List[Point],
  usage: String,
  poiId: Option[Int],
)

final case class Plaza(
  center: Point,
  radius: Int,
)

final case class District(
  id: Int,
  roll: Int,
  districtType: String,
  alignment: String,
  seatOfGovernment: Boolean,
  position: Point,
  boundary: List[Point],
  plazas: List[Plaza],
  pointsOfInterest: List[PointOfInterest],
  buildings: List[Building],
)

final case class Settlement(
  name: String,
  settlementType: SettlementType,
  alignment: String,
  districts: List[District],
  seatOfGovernment: Int,
  layout: SettlementLayout,
)

object Settlement {
  implicit val pointEncoder: JsonEncoder[Point] = DeriveJsonEncoder.gen[Point]
  implicit val pointDecoder: JsonDecoder[Point] = DeriveJsonDecoder.gen[Point]
  implicit val settlementTypeEncoder: JsonEncoder[SettlementType] =
    DeriveJsonEncoder.gen[SettlementType]
  implicit val settlementTypeDecoder: JsonDecoder[SettlementType] =
    DeriveJsonDecoder.gen[SettlementType]
  implicit val settlementLayoutEncoder: JsonEncoder[SettlementLayout] =
    DeriveJsonEncoder.gen[SettlementLayout]
  implicit val settlementLayoutDecoder: JsonDecoder[SettlementLayout] =
    DeriveJsonDecoder.gen[SettlementLayout]
  implicit val drinkEncoder: JsonEncoder[Drink] = DeriveJsonEncoder.gen[Drink]
  implicit val drinkDecoder: JsonDecoder[Drink] = DeriveJsonDecoder.gen[Drink]
  implicit val foodEncoder: JsonEncoder[Food] = DeriveJsonEncoder.gen[Food]
  implicit val foodDecoder: JsonDecoder[Food] = DeriveJsonDecoder.gen[Food]
  implicit val tavernEncoder: JsonEncoder[Tavern] = DeriveJsonEncoder.gen[Tavern]
  implicit val tavernDecoder: JsonDecoder[Tavern] = DeriveJsonDecoder.gen[Tavern]
  implicit val npcEncoder: JsonEncoder[Npc] = DeriveJsonEncoder.gen[Npc]
  implicit val npcDecoder: JsonDecoder[Npc] = DeriveJsonDecoder.gen[Npc]
  implicit val shopEncoder: JsonEncoder[Shop] = DeriveJsonEncoder.gen[Shop]
  implicit val shopDecoder: JsonDecoder[Shop] = DeriveJsonDecoder.gen[Shop]
  implicit val poiEncoder: JsonEncoder[PointOfInterest] = DeriveJsonEncoder.gen[PointOfInterest]
  implicit val poiDecoder: JsonDecoder[PointOfInterest] = DeriveJsonDecoder.gen[PointOfInterest]
  implicit val buildingEncoder: JsonEncoder[Building] = DeriveJsonEncoder.gen[Building]
  implicit val buildingDecoder: JsonDecoder[Building] = DeriveJsonDecoder.gen[Building]
  implicit val plazaEncoder: JsonEncoder[Plaza] = DeriveJsonEncoder.gen[Plaza]
  implicit val plazaDecoder: JsonDecoder[Plaza] = DeriveJsonDecoder.gen[Plaza]
  implicit val districtEncoder: JsonEncoder[District] = DeriveJsonEncoder.gen[District]
  implicit val districtDecoder: JsonDecoder[District] = DeriveJsonDecoder.gen[District]
  implicit val settlementEncoder: JsonEncoder[Settlement] = DeriveJsonEncoder.gen[Settlement]
  implicit val settlementDecoder: JsonDecoder[Settlement] = DeriveJsonDecoder.gen[Settlement]
}
