package servers

import zio.ZIO
import zio.test.{ZIOSpecDefault, assertTrue, suite, test}

object DungeonServerSpec extends ZIOSpecDefault {
  override def spec =
    suite("DungeonServer")(
      test("randomDungeon produces rooms, an objective, and entrances") {
        for {
          dungeon <- DungeonServer().randomDungeon
        } yield assertTrue(
          dungeon.rooms.nonEmpty,
          dungeon.rooms.map(_.id).distinct.size == dungeon.rooms.size,
          dungeon.rooms.exists(_.objectiveRoom),
          dungeon.rooms.forall(room => room.width > 0 && room.height > 0),
          dungeon.layout.outline.size >= 3,
          dungeon.layout.entrances.nonEmpty
        )
      },
      test("renderDungeonPdf returns a non-empty PDF payload") {
        for {
          server <- ZIO.succeed(DungeonServer())
          dungeon <- server.randomDungeon
          pdf <- server.renderDungeonPdf(dungeon)
        } yield assertTrue(pdf.nonEmpty)
      }
    )
}
