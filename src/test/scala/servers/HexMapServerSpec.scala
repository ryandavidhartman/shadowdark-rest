package servers

import org.apache.pdfbox.Loader
import zio.ZIO
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
      test("nextMap adds a new hex when moving beyond the initial ring") {
        val server = HexMapServer()
        for {
          map <- server.randomMap
          moved <- server.nextMap(map, "E")
          expanded <- server.nextMap(moved, "E")
        } yield assertTrue(
          moved.hexes.size == 7,
          expanded.hexes.size == 8,
          expanded.activeColumn == 2,
          expanded.activeRow == 0
        )
      },
      test("renderHexMapPdf returns a non-empty PDF payload") {
        for {
          server <- ZIO.succeed(HexMapServer())
          map <- server.randomMap
          pdf <- server.renderHexMapPdf(map)
          doc <- ZIO.attempt(Loader.loadPDF(pdf))
          pages <- ZIO.attempt(doc.getNumberOfPages)
          _ <- ZIO.attempt(doc.close())
        } yield assertTrue(
          pdf.nonEmpty,
          new String(pdf.take(5), java.nio.charset.StandardCharsets.US_ASCII) == "%PDF-",
          pages >= 1
        )
      },
      test("renderHexMapPng returns a non-empty PNG payload") {
        for {
          server <- ZIO.succeed(HexMapServer())
          map <- server.randomMap
          png <- server.renderHexMapPng(map)
        } yield assertTrue(
          png.nonEmpty,
          png.take(8).sameElements(Array(0x89.toByte, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a))
        )
      }
    )
}
