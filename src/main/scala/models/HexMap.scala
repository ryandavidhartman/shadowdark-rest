package models

import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder, JsonDecoder, JsonEncoder}

final case class HexMapLayout(
  columns: Int,
  rows: Int,
)

final case class HexPointOfInterest(
  id: Int,
  location: String,
  development: String,
  cataclysm: Option[String],
)

final case class HexCell(
  id: Int,
  column: Int,
  row: Int,
  terrain: String,
  terrainStep: Int,
  pointOfInterest: Option[HexPointOfInterest],
)

final case class HexMap(
  name: String,
  climate: String,
  dangerLevel: String,
  layout: HexMapLayout,
  hexes: List[HexCell],
)

object HexMap {
  implicit val layoutEncoder: JsonEncoder[HexMapLayout] = DeriveJsonEncoder.gen[HexMapLayout]
  implicit val layoutDecoder: JsonDecoder[HexMapLayout] = DeriveJsonDecoder.gen[HexMapLayout]
  implicit val poiEncoder: JsonEncoder[HexPointOfInterest] = DeriveJsonEncoder.gen[HexPointOfInterest]
  implicit val poiDecoder: JsonDecoder[HexPointOfInterest] = DeriveJsonDecoder.gen[HexPointOfInterest]
  implicit val cellEncoder: JsonEncoder[HexCell] = DeriveJsonEncoder.gen[HexCell]
  implicit val cellDecoder: JsonDecoder[HexCell] = DeriveJsonDecoder.gen[HexCell]
  implicit val mapEncoder: JsonEncoder[HexMap] = DeriveJsonEncoder.gen[HexMap]
  implicit val mapDecoder: JsonDecoder[HexMap] = DeriveJsonDecoder.gen[HexMap]
}
