package models

import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder, JsonDecoder, JsonEncoder}

final case class DungeonSize(
    name: String,
    diceCount: Int,
    dieSides: Int
)

final case class DungeonRoom(
    id: Int,
    roll: Int,
    roomType: String,
    details: List[String],
    position: Point,
    width: Int,
    height: Int,
    objectiveRoom: Boolean
)

final case class DungeonCorridor(
    start: Point,
    end: Point
)

final case class DungeonLayout(
    width: Int,
    height: Int,
    gridSize: Int,
    outline: List[Point],
    entrances: List[Point],
    seed: Long
)

final case class Dungeon(
    name: String,
    siteType: String,
    size: DungeonSize,
    dangerLevel: String,
    rooms: List[DungeonRoom],
    corridors: List[DungeonCorridor],
    layout: DungeonLayout
)

object Dungeon {
  implicit val pointEncoder: JsonEncoder[Point] = DeriveJsonEncoder.gen[Point]
  implicit val pointDecoder: JsonDecoder[Point] = DeriveJsonDecoder.gen[Point]
  implicit val sizeEncoder: JsonEncoder[DungeonSize] = DeriveJsonEncoder.gen[DungeonSize]
  implicit val sizeDecoder: JsonDecoder[DungeonSize] = DeriveJsonDecoder.gen[DungeonSize]
  implicit val roomEncoder: JsonEncoder[DungeonRoom] = DeriveJsonEncoder.gen[DungeonRoom]
  implicit val roomDecoder: JsonDecoder[DungeonRoom] = DeriveJsonDecoder.gen[DungeonRoom]
  implicit val corridorEncoder: JsonEncoder[DungeonCorridor] = DeriveJsonEncoder.gen[DungeonCorridor]
  implicit val corridorDecoder: JsonDecoder[DungeonCorridor] = DeriveJsonDecoder.gen[DungeonCorridor]
  implicit val layoutEncoder: JsonEncoder[DungeonLayout] = DeriveJsonEncoder.gen[DungeonLayout]
  implicit val layoutDecoder: JsonDecoder[DungeonLayout] = DeriveJsonDecoder.gen[DungeonLayout]
  implicit val dungeonEncoder: JsonEncoder[Dungeon] = DeriveJsonEncoder.gen[Dungeon]
  implicit val dungeonDecoder: JsonDecoder[Dungeon] = DeriveJsonDecoder.gen[Dungeon]
}
