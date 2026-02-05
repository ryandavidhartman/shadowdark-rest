package servers

import zio.test.{ZIOSpecDefault, assertTrue, suite, test}

object HexMapServerSpec extends ZIOSpecDefault {
  override def spec =
    suite("HexMapServer")(
      test("randomMap creates the initial 7-hex map with a centered active hex") {
        for {
          map <- HexMapServer().randomMap
        } yield {
          val uniqueCoords = map.hexes.map(h => (h.column, h.row)).distinct
          assertTrue(
            map.hexes.size == 7,
            uniqueCoords.size == 7,
            map.hexes.exists(h => h.column == 0 && h.row == 0),
            map.activeColumn == 0,
            map.activeRow == 0
          )
        }
      },
      test("nextMap moves the active hex without adding a new hex when neighbor exists") {
        for {
          map <- HexMapServer().randomMap
          updated <- HexMapServer().nextMap(map, "NE")
        } yield assertTrue(
          updated.activeColumn == 0,
          updated.activeRow == -1,
          updated.hexes.size == 7
        )
      },
      test("renderHexMapPdf returns a non-empty PDF payload") {
        for {
          server <- ZIO.succeed(HexMapServer())
          map <- server.randomMap
          pdf <- server.renderHexMapPdf(map)
        } yield assertTrue(pdf.nonEmpty)
      },
      test("renderHexMapPng returns a non-empty PNG payload") {
        for {
          server <- ZIO.succeed(HexMapServer())
          map <- server.randomMap
          png <- server.renderHexMapPng(map)
        } yield assertTrue(png.nonEmpty)
      }
    )
}
